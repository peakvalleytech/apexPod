package de.danoeh.apexpod.core.storage.autodownload.impl

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import de.danoeh.apexpod.core.preferences.UserPreferences
import de.danoeh.apexpod.core.storage.AutomaticDownloadAlgorithm
import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.core.util.FeedItemUtil
import de.danoeh.apexpod.core.util.NetworkUtils
import de.danoeh.apexpod.core.util.PowerUtils
import de.danoeh.apexpod.model.feed.AutoDownload
import de.danoeh.apexpod.model.feed.Feed
import de.danoeh.apexpod.model.feed.FeedItem
import de.danoeh.apexpod.model.feed.FeedPreferences

class AutoDownloadServiceImpl() {
    private val TAG = "AutoDownlaodService"

    fun autoDownloadUndownloadedItems(context: Context) : Runnable {
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
                            // if getAll
                            // get all episodes
                            // sort for newest or oldest
                            // Retrieve update count
                            // Download if necessary
                            // else
                            val items = feed.items
                            val filteredItems = selectFeedItems(
                                autodownloadprefs = preferences.autoDownloadPreferences,
                                items
                            )
                            itemsToDownload.addAll(filteredItems)
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
            var selectedItems = mutableListOf<FeedItem>()
            if (!autodownloadprefs.isIncludeAll) {
                items.forEach {
                    if (it.playState == FeedItem.NEW) {
                        selectedItems.add(it)
                    }
                }
            } else {
                items.forEach {
                    if (it.playState == FeedItem.NEW
                        || it.playState == FeedItem.UNPLAYED) {
                        selectedItems.add(it)
                    }
                }
            }
            selectedItems.sortBy { feedItem -> feedItem.pubDate }
            if (autodownloadprefs.isNewestFirst) {
                selectedItems.reverse()
            }


            if (autodownloadprefs.cacheSize > selectedItems.size) {
                selectedItems = selectedItems.subList(0, selectedItems.size)
            }

            val downloadAbleSelectedItems = filterDownloadable(selectedItems, autodownloadprefs.cacheSize)

            return downloadAbleSelectedItems
        }

        private fun filterDownloadable(
            selectedItems: MutableList<FeedItem>,
            limit : Int
        ): MutableList<FeedItem> {
            var i = 0
            val downloadAbleSelectedItems: MutableList<FeedItem> = mutableListOf()

            while (downloadAbleSelectedItems.size < limit && i < selectedItems.size) {
                val item = selectedItems.get(i)
                if (item.isAutoDownloadable() && !FeedItemUtil.isPlaying(item.getMedia())
                    && !item.getFeed().isLocalFeed()
                ) {
                    downloadAbleSelectedItems.add(item)
                }
                ++i
            }
            return downloadAbleSelectedItems
        }
    }
}