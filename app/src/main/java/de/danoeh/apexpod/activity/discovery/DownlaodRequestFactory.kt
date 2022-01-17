package de.danoeh.apexpod.activity.discovery

import android.content.Context
import de.danoeh.apexpod.core.service.download.DownloadRequest
import de.danoeh.apexpod.core.util.FileNameGenerator
import de.danoeh.apexpod.core.util.URLChecker
import de.danoeh.apexpod.model.feed.Feed
import de.danoeh.apexpod.model.feed.FeedPreferences
import de.danoeh.apexpod.model.feed.VolumeAdaptionSetting
import java.io.File

class DownlaodRequestFactory(
) {
    var feed : Feed? = null
    fun create(
            downloadUrl: String,
            destFile: File,
            username: String?,
            password: String?
        ): DownloadRequest? {
            feed = Feed(downloadUrl, null)
            if (username != null && password != null) {
                feed?.setPreferences(
                    FeedPreferences(
                        0, false, FeedPreferences.AutoDeleteAction.GLOBAL,
                        VolumeAdaptionSetting.OFF, username, password
                    )
                )
            }
            val fileUrl: String = File(
                destFile,
                FileNameGenerator.generateFileName(feed?.getDownload_url())
            ).toString()
            feed?.setFile_url(fileUrl)
            var request : DownloadRequest? = null
            feed?.let {
                request = DownloadRequest(
                    it.getFile_url(),
                    it.getDownload_url(), "OnlineFeed", 0, Feed.FEEDFILETYPE_FEED, username, password,
                    true, null, true
                )
            }
            return request
        }
}