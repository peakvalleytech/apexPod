package de.danoeh.apexpod.model.feed.feedItemFilter

import de.danoeh.apexpod.model.feed.FeedItem

interface FeedItemFilter2 {
    fun filter(items : List<FeedItem>) : List<FeedItem>
}