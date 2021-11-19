package de.danoeh.apexpod.core.storage.autodelete

import de.danoeh.apexpod.model.feed.FeedItem

interface AutoDeleteFilter {
    fun filter(feedItems: List<FeedItem>) : List<FeedItem>

    interface Builder {
        fun addAutoDeleteRule(rule : AutoDeleteRule) : Builder
        fun build() : AutoDeleteFilter
    }
}