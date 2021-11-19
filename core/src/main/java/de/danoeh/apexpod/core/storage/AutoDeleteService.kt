package de.danoeh.apexpod.core.storage

import android.content.Context
import de.danoeh.apexpod.core.storage.autodelete.AutoDeleteFilter
import de.danoeh.apexpod.model.feed.FeedItem

class AutoDeleteService(
    val context: Context,
    val dbReader: DBReader,
    val dbWriter: DBWriter,
    val autoDeleteFilter: AutoDeleteFilter) {
    fun start() {
        var downloadedItems = DBReader.getDownloadedItems()

        var itemsToDelete = autoDeleteFilter.filter(downloadedItems)

        for (item in itemsToDelete) {
            item.media?.let { DBWriter.deleteFeedMediaOfItem(context, it.id) }
        }
    }
}