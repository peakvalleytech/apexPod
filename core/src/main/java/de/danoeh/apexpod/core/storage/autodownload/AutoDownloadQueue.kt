package de.danoeh.apexpod.core.storage.autodownload

import de.danoeh.apexpod.core.util.FeedItemUtil
import de.danoeh.apexpod.model.feed.FeedItem

class AutoDownloadQueue(val items : MutableList<FeedItem>) {

    fun getUnplayedItems(): AutoDownloadQueue {
        val unplayedItems = mutableListOf<FeedItem>()

        items.forEach {
            if (it.playState == FeedItem.NEW
                || it.playState == FeedItem.UNPLAYED
            ) {
                unplayedItems.add(it)
            }
        }

        return AutoDownloadQueue(unplayedItems)
    }

    /**
     * Gets the next unplayed items that can be downloaded
     */
    fun getNextDownloads(
        limit: Int
    ): AutoDownloadQueue {
        var i = 0
        val downloadAbleSelectedItems: MutableList<FeedItem> = mutableListOf()

        while (downloadAbleSelectedItems.size < limit && i < items.size) {
            val item = items.get(i)
            if (item.media != null && !FeedItemUtil.isPlaying(item.getMedia())
                && !item.getFeed().isLocalFeed()
            ) {
                downloadAbleSelectedItems.add(item)
            }
            ++i
        }
        return AutoDownloadQueue(downloadAbleSelectedItems)
    }

    fun sortByNewest(reverse: Boolean) {
        items.sortBy { feedItem -> feedItem.pubDate }
        if (!reverse) {
            items.reverse()
        }
    }

    fun size(): Int {
        return items.size
    }

    fun subList(start: Int, size: Int): AutoDownloadQueue {
        return AutoDownloadQueue(items.subList(start, size))
    }

    fun toList() : List<FeedItem> {
        return items
    }
}