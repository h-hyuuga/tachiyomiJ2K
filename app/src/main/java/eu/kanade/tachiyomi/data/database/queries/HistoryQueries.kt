package eu.kanade.tachiyomi.data.database.queries

import com.pushtorefresh.storio.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio.sqlite.queries.RawQuery
import eu.kanade.tachiyomi.data.database.DbProvider
import eu.kanade.tachiyomi.data.database.models.History
import eu.kanade.tachiyomi.data.database.models.MangaChapterHistory
import eu.kanade.tachiyomi.data.database.resolvers.HistoryLastReadPutResolver
import eu.kanade.tachiyomi.data.database.resolvers.HistoryUpsertResolver
import eu.kanade.tachiyomi.data.database.resolvers.MangaChapterHistoryGetResolver
import eu.kanade.tachiyomi.data.database.tables.HistoryTable
import eu.kanade.tachiyomi.util.lang.sqLite

interface HistoryQueries : DbProvider {

    /**
     * Insert history into database
     * @param history object containing history information
     */
//    fun insertHistory(history: History) = db.put().`object`(history).prepare()

//    /**
//     * Returns history of recent manga containing last read chapter in 25s
//     * @param date recent date range
//     * @offset offset the db by
//     */
//    fun getRecentManga(date: Date, offset: Int = 0, search: String = "") = db.get()
//        .listOfObjects(MangaChapterHistory::class.java)
//        .withQuery(
//            RawQuery.builder()
//                .query(getRecentMangasQuery(offset, search.sqLite))
//                .args(date.time)
//                .observesTables(HistoryTable.TABLE)
//                .build()
//        )
//        .withGetResolver(MangaChapterHistoryGetResolver.INSTANCE)
//        .prepare()

    /**
     * Returns history of recent manga containing last read chapter in 25s
     * @param date recent date range
     * @offset offset the db by
     */
    fun getRecentMangaLimit(search: String = "", offset: Int, isResuming: Boolean) = db.get()
        .listOfObjects(MangaChapterHistory::class.java)
        .withQuery(
            RawQuery.builder()
                .query(getRecentMangasLimitQuery(search.sqLite, offset, isResuming))
//                .args(date.time, startDate.time)
                .observesTables(HistoryTable.TABLE)
                .build(),
        )
        .withGetResolver(MangaChapterHistoryGetResolver.INSTANCE)
        .prepare()

    /**
     * Returns history of recent manga containing last read chapter in 25s
     * @param date recent date range
     * @offset offset the db by
     */
    fun getHistoryUngrouped(search: String = "", offset: Int, isResuming: Boolean) = db.get()
        .listOfObjects(MangaChapterHistory::class.java)
        .withQuery(
            RawQuery.builder()
                .query(getRecentHistoryUngrouped(search.sqLite, offset, isResuming))
//                .args(date.time, startDate.time)
                .observesTables(HistoryTable.TABLE)
                .build(),
        )
        .withGetResolver(MangaChapterHistoryGetResolver.INSTANCE)
        .prepare()

    /**
     * Returns history of recent manga containing last read chapter in 25s
     * @param date recent date range
     * @offset offset the db by
     */
    fun getAllRecentsTypes(search: String = "", includeRead: Boolean, endless: Boolean, offset: Int, isResuming: Boolean) = db.get()
        .listOfObjects(MangaChapterHistory::class.java)
        .withQuery(
            RawQuery.builder()
                .query(
                    getAllRecentsType(
                        search.sqLite,
                        includeRead,
                        endless,
                        offset,
                        isResuming,
                    ),
                )
//                .args(date.time, startDate.time)
                .observesTables(HistoryTable.TABLE)
                .build(),
        )
        .withGetResolver(MangaChapterHistoryGetResolver.INSTANCE)
        .prepare()

    fun getHistoryByMangaId(mangaId: Long) = db.get()
        .listOfObjects(History::class.java)
        .withQuery(
            RawQuery.builder()
                .query(getHistoryByMangaId())
                .args(mangaId)
                .observesTables(HistoryTable.TABLE)
                .build(),
        )
        .prepare()

    fun getHistoryByChapterUrl(chapterUrl: String) = db.get()
        .`object`(History::class.java)
        .withQuery(
            RawQuery.builder()
                .query(getHistoryByChapterUrl())
                .args(chapterUrl)
                .observesTables(HistoryTable.TABLE)
                .build(),
        )
        .prepare()

    /**
     * Updates the history last read.
     * Inserts history object if not yet in database
     * @param history history object
     */
    fun updateHistoryLastRead(history: History) = db.put()
        .`object`(history)
        .withPutResolver(HistoryLastReadPutResolver())
        .prepare()

    /**
     * Updates the history last read.
     * Inserts history object if not yet in database
     * @param historyList history object list
     */
    fun upsertHistoryLastRead(historyList: List<History>) = db.put()
        .objects(historyList)
        .withPutResolver(HistoryUpsertResolver())
        .prepare()

    /**
     * Updates the history last read.
     * Inserts history object if not yet in database
     * @param historyList history object list
     */
    fun updateHistoryLastRead(historyList: List<History>) = db.put()
        .objects(historyList)
        .withPutResolver(HistoryLastReadPutResolver())
        .prepare()

    fun deleteHistory() = db.delete()
        .byQuery(
            DeleteQuery.builder()
                .table(HistoryTable.TABLE)
                .build(),
        )
        .prepare()

    fun deleteHistoryNoLastRead() = db.delete()
        .byQuery(
            DeleteQuery.builder()
                .table(HistoryTable.TABLE)
                .where("${HistoryTable.COL_LAST_READ} = ?")
                .whereArgs(0)
                .build(),
        )
        .prepare()
}
