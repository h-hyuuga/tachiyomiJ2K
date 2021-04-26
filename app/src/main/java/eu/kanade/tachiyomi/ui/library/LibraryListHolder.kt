package eu.kanade.tachiyomi.ui.library

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import coil.api.clear
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.image.coil.loadLibraryManga
import eu.kanade.tachiyomi.databinding.MangaListItemBinding
import eu.kanade.tachiyomi.util.system.dpToPx
import eu.kanade.tachiyomi.util.view.updateLayoutParams
import eu.kanade.tachiyomi.util.view.visible

/**
 * Class used to hold the displayed data of a manga in the library, like the cover or the binding.title.
 * All the elements from the layout file "item_library_list" are available in this class.
 *
 * @param view the inflated view for this holder.
 * @param adapter the adapter handling this holder.
 * @constructor creates a new library holder.
 */

class LibraryListHolder(
    private val view: View,
    adapter: LibraryCategoryAdapter
) : LibraryHolder(view, adapter) {

    private val binding = MangaListItemBinding.bind(view)

    /**
     * Method called from [LibraryCategoryAdapter.onBindViewHolder]. It updates the data for this
     * holder with the given manga.
     *
     * @param item the manga item to bind.
     */
    override fun onSetValues(item: LibraryItem) {
        binding.title.visible()
        binding.constraintLayout.minHeight = 56.dpToPx
        if (item.manga.isBlank()) {
            binding.constraintLayout.minHeight = 0
            binding.constraintLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height = ViewGroup.MarginLayoutParams.WRAP_CONTENT
            }
            if (item.manga.status == -1) {
                binding.title.text = null
                binding.title.isVisible = false
            } else {
                binding.title.text = itemView.context.getString(R.string.category_is_empty)
            }
            binding.title.textAlignment = View.TEXT_ALIGNMENT_CENTER
            binding.card.isVisible = false
            binding.unreadDownloadBadge.badgeView.isVisible = false
            binding.padding.isVisible = false
            binding.subtitle.isVisible = false
            return
        }
        binding.constraintLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            height = 52.dpToPx
        }
        binding.padding.visible()
        binding.card.visible()
        binding.title.textAlignment = View.TEXT_ALIGNMENT_TEXT_START

        // Update the binding.title of the manga.
        binding.title.text = item.manga.title
        setUnreadBadge(binding.unreadDownloadBadge.badgeView, item)

        binding.subtitle.text = item.manga.author?.trim()
        binding.title.post {
            if (binding.title.text == item.manga.title) {
                binding.subtitle.isVisible = binding.title.lineCount == 1 && !item.manga.author.isNullOrBlank()
            }
        }

        // Update the cover.
        if (item.manga.thumbnail_url == null) {
            binding.coverThumbnail.clear()
        } else {
            item.manga.id ?: return
            binding.coverThumbnail.loadLibraryManga(item.manga)
        }
    }

    override fun onActionStateChanged(position: Int, actionState: Int) {
        super.onActionStateChanged(position, actionState)
        if (actionState == 2) {
            binding.card.isDragged = true
        }
    }

    override fun onItemReleased(position: Int) {
        super.onItemReleased(position)
        binding.card.isDragged = false
    }
}
