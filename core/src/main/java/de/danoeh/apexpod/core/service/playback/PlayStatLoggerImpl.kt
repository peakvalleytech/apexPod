package de.danoeh.apexpod.core.service.playback

import android.util.Log
import de.danoeh.apexpod.core.util.Converter
import de.danoeh.apexpod.core.util.DateFormatter
import de.danoeh.apexpod.model.stats.PlayingStat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class PlayStatLoggerImpl : PlayStatLogger {
    private val TAG = "PlayStatLoggerImpl"
    private var currPlayStat : PlayingStat? = null
    override fun startPlayStat(startTime: Long, startPosition: Int, feedMediaId: Long) {
        Log.d(TAG, "Starting new PlayStat at ${prettyMillis(startTime)} at position  ${Converter.getDurationStringLong(startPosition)}" )
        // If currPlayStat is not null
        // callEndPlayStat
        // ...
    }

    override fun endPlayStat(endTime: Long, endPosition: Int, feedMediaId: Long) {
        Log.d(TAG, "Ending PlayStat at ${prettyMillis(endTime)} at position ${Converter.getDurationStringLong(endPosition)}")
        // If currPlayStat is not null
        // ...
        // set currPlayStat to null
    }

    private fun prettyMillis(millis : Long) : String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val dateTimeFormatter = SimpleDateFormat.getDateTimeInstance()
        return dateTimeFormatter.format(cal.time)
    }
}