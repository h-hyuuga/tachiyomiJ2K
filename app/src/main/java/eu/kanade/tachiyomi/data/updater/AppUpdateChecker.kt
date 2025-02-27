package eu.kanade.tachiyomi.data.updater

import android.content.Context
import android.os.Build
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.await
import eu.kanade.tachiyomi.network.parseAs
import eu.kanade.tachiyomi.util.system.withIOContext
import uy.kohesive.injekt.injectLazy
import java.util.Date
import java.util.concurrent.TimeUnit

class AppUpdateChecker {

    private val networkService: NetworkHelper by injectLazy()
    private val preferences: PreferencesHelper by injectLazy()

    suspend fun checkForUpdate(context: Context, isUserPrompt: Boolean = false, doExtrasAfterNewUpdate: Boolean = true): AppUpdateResult {
        // Limit checks to once a day at most
        if (!isUserPrompt && Date().time < preferences.lastAppCheck().get() + TimeUnit.DAYS.toMillis(1)) {
            return AppUpdateResult.NoNewUpdate
        }

        return withIOContext {
            val result = if (preferences.checkForBetas().get()) {
                networkService.client
                    .newCall(GET("https://api.github.com/repos/$GITHUB_REPO/releases"))
                    .await()
                    .parseAs<List<GithubRelease>>()
                    .let {
                        val release = it.firstOrNull() ?: return@let AppUpdateResult.NoNewUpdate
                        preferences.lastAppCheck().set(Date().time)

                        // Check if latest version is different from current version
                        if (isNewVersion(release.version)) {
                            AppUpdateResult.NewUpdate(release)
                        } else {
                            AppUpdateResult.NoNewUpdate
                        }
                    }
            } else {
                networkService.client
                    .newCall(GET("https://api.github.com/repos/$GITHUB_REPO/releases/latest"))
                    .await()
                    .parseAs<GithubRelease>()
                    .let {
                        preferences.lastAppCheck().set(Date().time)

                        // Check if latest version is different from current version
                        if (isNewVersion(it.version)) {
                            AppUpdateResult.NewUpdate(it)
                        } else {
                            AppUpdateResult.NoNewUpdate
                        }
                    }
            }
            if (doExtrasAfterNewUpdate && result is AppUpdateResult.NewUpdate) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    preferences.appShouldAutoUpdate() != AutoAppUpdaterJob.NEVER
                ) {
                    AutoAppUpdaterJob.setupTask(context)
                }
                AppUpdateNotifier(context).promptUpdate(result.release)
            }

            result
        }
    }

    private fun isNewVersion(versionTag: String): Boolean {
        // Removes prefixes like "r" or "v"
        val newVersion = versionTag.replace("[^\\d.-]".toRegex(), "")
        val oldVersion = BuildConfig.VERSION_NAME.replace("[^\\d.-]".toRegex(), "")
        val newPreReleaseVer = newVersion.split("-")
        val oldPreReleaseVer = oldVersion.split("-")
        val newSemVer = newPreReleaseVer.first().split(".").map { it.toInt() }
        val oldSemVer = oldPreReleaseVer.first().split(".").map { it.toInt() }

        oldSemVer.mapIndexed { index, i ->
            if (newSemVer.getOrElse(index) { i } > i) {
                return true
            }
        }
        // For cases of extreme patch versions (new: 1.2.3.1 vs old: 1.2.3, return true)
        return if (newSemVer.size > oldSemVer.size) {
            true
        } else {
            // For production versions from beta (new: 1.2.3 vs old: 1.2.3-b1, return true)
            (newPreReleaseVer.getOrNull(1) != null) != (oldPreReleaseVer.getOrNull(1) != null)
        }
    }
}

val RELEASE_TAG: String by lazy {
    "v${BuildConfig.VERSION_NAME}"
}

const val GITHUB_REPO: String = "Jays2Kings/tachiyomiJ2K"

val RELEASE_URL = "https://github.com/$GITHUB_REPO/releases/tag/$RELEASE_TAG"
