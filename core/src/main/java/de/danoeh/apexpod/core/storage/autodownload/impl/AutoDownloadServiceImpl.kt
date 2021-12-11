package de.danoeh.apexpod.core.storage.autodownload.impl

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import de.danoeh.apexpod.core.preferences.UserPreferences
import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.core.storage.DownloadRequestException
import de.danoeh.apexpod.core.storage.DownloadRequester
import de.danoeh.apexpod.core.storage.autodownload.AutoDownloadQueue
import de.danoeh.apexpod.core.util.NetworkUtils
import de.danoeh.apexpod.core.util.PowerUtils
import de.danoeh.apexpod.model.feed.AutoDownload
import de.danoeh.apexpod.model.feed.Feed
import de.danoeh.apexpod.model.feed.FeedItem

class AutoDownloadServiceImpl() {
    private val TAG = "AutoDownlaodService"

    fun autoDownloadUndownloadedItems(context: Context): Runnable {
        return object : Runnable {
            override fun run() {
                // true if we should auto download based on network status
                val networkShouldAutoDl = NetworkUtils.autodownloadNetworkAvailable()
                        && UserPreferences.isEnableAutodownload()

                // true if we should auto download based on power status
                val powerShouldAutoDl = (PowerUtils.deviceCharging(context)
                        || UserPreferences.isEnableAutodownloadOnBattery())

                if (networkShouldAutoDl && powerShouldAutoDl) {
                    Log.d(
                        TAG,
                        "Performing auto-dl of undownloaded episodes"
                    )
                    val feeds: List<Feed> = DBReader.getFeedList()
                    val itemsToDownload = mutableListOf<FeedItem>()
                    for (feed in feeds) {
                        val preferences = feed.preferences
                        if (preferences.autoDownload) {

                            val items = DBReader.getFeedItemList(feed)
                            val filteredItems = selectFeedItems(
                                autodownloadprefs = preferences.autoDownloadPreferences,
                                items
                            )
                            itemsToDownload.addAll(filteredItems)
                        }
                    }
                    if (itemsToDownload.size > 0) {
                        try {
                            DownloadRequester.getInstance()
                                .downloadMedia(
                                    context,
                                    false,
                                    itemsToDownload
                                )
                        } catch (e: DownloadRequestException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Selects the feed items to auto download
         * @param autodownloadprefs the autodownloadprefs that holds auto download options
         * used to deteremine which items to select
         * @param feeditems the feeditems to select from
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun selectFeedItems(autodownloadprefs: AutoDownload, items: List<FeedItem>)
                : List<FeedItem> {
            var autoDownloadQueue = AutoDownloadQueue(items.toMutableList())
            autoDownloadQueue = autoDownloadQueue.getUnplayedItems()

            autoDownloadQueue.sortByNewest(!autodownloadprefs.isNewestFirst)

            var downloadAbleSelectedItems =
                autoDownloadQueue.getNextDownloads(autodownloadprefs.cacheSize)

            val undownloadedItems = downloadAbleSelectedItems.items.filter {
                !it.isDownloaded
            }

            return undownloadedItems
        }
    }
}