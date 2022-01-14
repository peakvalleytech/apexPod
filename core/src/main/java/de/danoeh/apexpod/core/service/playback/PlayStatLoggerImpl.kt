package de.danoeh.apexpod.core.service.playback

import android.util.Log
import de.danoeh.apexpod.core.storage.repository.PlayStatsRepository
import de.danoeh.apexpod.core.util.Converter
import de.danoeh.apexpod.model.feed.FeedItem
import de.danoeh.apexpod.model.stats.PlayStat
import java.text.SimpleDateFormat
import java.util.*

class PlayStatLoggerImpl(
    playStatsRepository: PlayStatsRepository
) : PlayStatLogger {
    private val TAG = "PlayStatLoggerImpl"
    private var currPlayStat : PlayStat? = null
    private var playingFeedItem : FeedItem? = null
    override fun startPlayStat(startTime: Long, startPosition: Int, feedItem: FeedItem) {
        currPlayStat = PlayStat(0, 0, startTime, 0, startPosition, -1)
        playingFeedItem = feedItem
        Log.d(TAG, "Starting new PlayStat at ${prettyMillis(startTime)} at position  ${Converter.getDurationStringLong(startPosition)}" )
        Log.d(TAG, "Starting playback of ${playingFeedItem!!.title}")
    }

    override fun endPlayStat(endTime: Long, endPosition: Int, feedItem: FeedItem) {
        Log.d(TAG, "Ending PlayStat at ${prettyMillis(endTime)} at position ${Converter.getDurationStringLong(endPosition)}")
        Log.d(TAG, "Ending playback of ${feedItem.title}")
        if (currPlayStat != null && playingFeedItem != null) {
            currPlayStat!!.endTime = endTime
            currPlayStat!!.endPos = endPosition
        }
        // If currPlayStat is not null
        currPlayStat = null
        // set currPlayStat to null
        playingFeedItem = null
    }

    private fun prettyMillis(millis : Long) : String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val dateTimeFormatter = SimpleDateFormat.getDateTimeInstance()
        return dateTimeFormatter.format(cal.time)
    }
}