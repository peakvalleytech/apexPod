package de.danoeh.apexpod.core.storage.autodelete

import de.danoeh.apexpod.model.feed.FeedItem

/**
 * Handles deletion of downloaded feed items from storage
 */
interface AutoDeleteService {

    fun delete(feedItems : List<FeedItem>)


}