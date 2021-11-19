package de.danoeh.apexpod.core.storage.autodelete.impl

import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteRule
import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteFilter
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
}