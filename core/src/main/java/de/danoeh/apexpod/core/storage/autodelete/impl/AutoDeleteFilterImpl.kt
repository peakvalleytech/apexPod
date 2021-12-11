package de.danoeh.apexpod.core.storage.autodelete.impl

import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteRule
import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteFilter
import de.danoeh.apexpod.core.storage.autodelete.impl.rules.DurationAutoDeleteRule
import de.danoeh.apexpod.core.storage.autodelete.impl.rules.IncludeFavoritesAutoDeleteRule
import de.danoeh.apexpod.model.feed.FeedItem

class AutoDeleteFilterImpl internal constructor(val rules: MutableList<AutoDeleteRule>) : AutoDeleteFilter {
    override fun filter(feedItems: List<FeedItem>): List<FeedItem> {
        var filteredItems = mutableListOf<FeedItem>()

        feedItems.forEach({
            for (rule in rules) {
                if (rule.shouldDelete(it)) {
                    filteredItems.add(it)
                }
            }
        })

        return  filteredItems
    }

    class AutoDeleteFilterBuilderImpl() : AutoDeleteFilter.Builder {
        private val rules : MutableList<AutoDeleteRule> = mutableListOf()
        fun setDuration(hrsAfterPlayback: Int) : AutoDeleteFilterBuilderImpl {
            val durationAutoDeleteRule = DurationAutoDeleteRule(hrsAfterPlayback)
            addAutoDeleteRule(durationAutoDeleteRule)
            return this
        }

        fun keepFavorite(keep : Boolean) : AutoDeleteFilterBuilderImpl {
            val includeFavoritesAutoDeleteRule = IncludeFavoritesAutoDeleteRule()
            addAutoDeleteRule(includeFavoritesAutoDeleteRule)
            return this
        }

        fun keepPlaylisted(keep : Boolean) : AutoDeleteFilterBuilderImpl{
            if (!keep) {
//                val includeQueuedAutoDeleteRule = IncludeQueuedAutoDeleteRule()
//                addAutoDeleteRule(includeQueuedAutoDeleteRule)
            }
            return this
        }

        override fun build(): AutoDeleteFilter {
            return AutoDeleteFilterImpl(rules)
        }

        private fun addAutoDeleteRule(autoDeleteRule: AutoDeleteRule) {
            rules.add(autoDeleteRule)
        }
    }
}