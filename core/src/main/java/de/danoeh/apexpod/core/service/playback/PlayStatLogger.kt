package de.danoeh.apexpod.core.service.playback

import de.danoeh.apexpod.model.stats.PlayingStat

interface PlayStatLogger {


    /**
     * Starts a new PlayStat. If getCurrPlayStat is not null, ends currPlayStat before
     * starting a new one.
     */
    fun startPlayStat(startTime : Long, starPosition: Int, feedMediaId : Long)
    fun endPlayStat(endTime : Long, endPosition : Int, feedMediaId: Long)
}