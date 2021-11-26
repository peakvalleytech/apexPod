package de.danoeh.apexpod.core.storage.autodownload.impl

import android.content.Context
import de.danoeh.apexpod.core.preferences.UserPreferences
import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.core.util.NetworkUtils
import de.danoeh.apexpod.model.feed.Feed

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
                    if (preferences.autoDownload)
                        // if getAll
                            // get all episodes
                            // sort for newest or oldest
                            // Retrieve update count
                            // Download if necessary
                        // else
                }
            }
        }
    }

}