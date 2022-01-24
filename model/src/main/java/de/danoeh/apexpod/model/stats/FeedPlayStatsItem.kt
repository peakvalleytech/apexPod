package de.danoeh.apexpod.model.stats

import de.danoeh.apexpod.model.feed.Feed

data class FeedPlayStatsItem(
    val feed : Feed?,
    val totalSpeedAdjustedListeningTime : Long = 0,
    val totalListeningTime : Long = 0,
    val episodesListenedTo : Int = 0,
    val totalDownloadSize : Long = 0,
    val downloadCount : Long = 0
) {
    fun addPlayStat(playStat : PlayStat) {

    }
}