package de.danoeh.apexpod.core.storage.autodelete

import de.danoeh.apexpod.model.feed.FeedItem

interface AutoDeleteRule {
    fun shouldDelete(feedItem : FeedItem) : Boolean
}