package de.danoeh.apexpod.core.storage

import de.danoeh.apexpod.model.feed.FeedItem
import de.danoeh.apexpod.model.feed.FeedMedia
import java.io.File

class FeedStatsCalculator {
    var feedPlayedTimeCountAll: Long = 0
    var feedPlayedTime: Long = 0
    var feedTotalTime: Long = 0
    var episodes: Long = 0
    var episodesStarted: Long = 0
    var episodesStartedIncludingMarked: Long = 0
    var totalDownloadSize: Long = 0
    var episodesDownloadCount: Long = 0

    fun calculate(feedId : Long) {
        var items = DBReader.getFeed(feedId)!!.items
        for (  item : FeedItem? in items)
        {
            val media: FeedMedia = item?.getMedia() ?: continue
            feedPlayedTime += (media.playedDuration / 1000).toLong()
            feedPlayedTimeCountAll += if (item.isPlayed()) {
                (media.duration / 1000).toLong()
            } else {
                (media.position / 1000).toLong()
            }
            if (media.playbackCompletionDate != null || media.playedDuration > 0) {
                episodesStarted++
            }
            if (item.isPlayed() || media.position != 0) {
                episodesStartedIncludingMarked++
            }
            feedTotalTime += (media.duration / 1000).toLong()
            if (media.isDownloaded) {
                totalDownloadSize += File(media.file_url).length()
                episodesDownloadCount++
            }
            episodes++
        }
    }
}