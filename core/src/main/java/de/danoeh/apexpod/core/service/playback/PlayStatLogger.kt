package de.danoeh.apexpod.core.service.playback

import de.danoeh.apexpod.model.feed.FeedItem

interface PlayStatLogger {


    /**
     * Starts a new PlayStat. If getCurrPlayStat is not null, ends currPlayStat before
     * starting a new one.
     */
    fun startPlayStat(startTime : Long, startPosition: Int, feedItem : FeedItem)
    fun endPlayStat(endTime : Long, endPosition : Int, feedItem: FeedItem)
}