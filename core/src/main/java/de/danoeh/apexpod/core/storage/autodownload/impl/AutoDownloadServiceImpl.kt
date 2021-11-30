package de.danoeh.apexpod.core.storage.autodownload.impl

import android.content.Context
import androidx.annotation.VisibleForTesting
import de.danoeh.apexpod.core.preferences.UserPreferences
import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.core.util.NetworkUtils
import de.danoeh.apexpod.model.feed.AutoDownload
import de.danoeh.apexpod.model.feed.Feed
import de.danoeh.apexpod.model.feed.FeedItem
import de.danoeh.apexpod.model.feed.FeedPreferences

class AutoDownloadServiceImpl() {
    private val TAG = "AutoDownlaodService"

    fun autoDownloadUndownloadedItems(context: Context) : Runnable {
        return object : Runnable {
            override fun run() {
                val networkShouldAutoDl = NetworkUtils.autodownloadNetworkAvailable()
                        && UserPreferences.isEnableAutodownload()

                val feeds : List<Feed> = DBReader.getFeedList()

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
                        selectFeedItems(
                            autodownloadprefs = preferences.autoDownloadPreferences,
                            items
                        )
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
                return selectedItems
            } else  {
                return selectedItems.subList(0, autodownloadprefs.cacheSize)
            }
            return selectedItems
        }
    }
}