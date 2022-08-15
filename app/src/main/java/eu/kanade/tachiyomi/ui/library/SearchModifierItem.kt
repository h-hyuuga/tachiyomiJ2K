package eu.kanade.tachiyomi.ui.library

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.databinding.SearchModifierItemBinding
import eu.kanade.tachiyomi.ui.base.holder.BaseFlexibleViewHolder

/**
 *
 * @property toggleShowAllCategories A function that toggles forceShowAllCategories in the LibraryPresenter
 *                                   Returns the new value of  forceShowAllCategories
 */
class SearchGlobalItem(val toggleShowAllCategories: () -> Boolean) : AbstractFlexibleItem<SearchGlobalItem.Holder>() {

    var string = ""
    var allCategoriesToggleIsVisible = false
    override fun getLayoutRes(): Int {
        return R.layout.search_modifier_item
    }

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
    ): Holder {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            width = MATCH_PARENT
        }
        return Holder(view, adapter)
    }

    override fun isSelectable(): Boolean {
        return false
    }

    override fun isSwipeable(): Boolean {
        return false
    }

    override fun isDraggable(): Boolean {
        return false
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: Holder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        holder.allCategoriesToggleIsVisible = allCategoriesToggleIsVisible
        holder.bind(string)
        val layoutParams = holder.itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams
        layoutParams?.isFullSpan = true
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return -100
    }

    class Holder(val view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>) :
        BaseFlexibleViewHolder(view, adapter, true) {

        private val binding = SearchModifierItemBinding.bind(view)
        var allCategoriesToggleIsVisible: Boolean by binding.allCategories.root::isVisible
        private var isForceShowingAllCategories = false
        init {
            binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                width = MATCH_PARENT
            }

            binding.global.button.setOnClickListener {
                val query = (adapter.getItem(flexibleAdapterPosition) as SearchGlobalItem).string
                (adapter as? LibraryCategoryAdapter)?.libraryListener?.globalSearch(query)
            }

            allCategoriesToggleIsVisible = false
            binding.allCategories.button.setOnClickListener {
                (adapter.getItem(flexibleAdapterPosition) as SearchGlobalItem).apply {
                    isForceShowingAllCategories = toggleShowAllCategories()
                }
            }
        }

        fun bind(string: String) {
            binding.global.button.text = view.context.getString(R.string.search_globally, string)
            binding.allCategories.button.text = view.context.getString(if (!isForceShowingAllCategories) R.string.search_all_categories else R.string.search_current_category, string)
        }

        override fun onLongClick(view: View?): Boolean {
            super.onLongClick(view)
            return false
        }
    }
}
