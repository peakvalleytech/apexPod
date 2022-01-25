package de.danoeh.apexpod.model.stats

import de.danoeh.apexpod.model.feed.Feed

data class FeedPlayStatsItem(
    val feed : Feed?,
    val totalSpeedAdjustedListeningTime : Long = 0,
    val totalListeningTime : Long = 0,
    val totalTime : Long = 0,
    val episodesListenedTo : Int = 0,
    val episodesStarted : Long = 0,
    val episodeCount : Long = 0,
    val totalDownloadSize : Long = 0,
    val downloadsCount : Long = 0
) {
}