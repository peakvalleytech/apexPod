package de.danoeh.apexpod.model.stats

import de.danoeh.apexpod.model.feed.Feed

data class FeedPlayStatsItem(
    val feed : Feed?,
    val totalSpeedAdjustedListeningTime : Long = 0,
    val totalListeningTime : Long = 0,
    val totalTime : Long = 0,
    val episodesListenedTo : Int = 0,
    val episodesStarted : Int = 0,
    val episodeCount : Int = 0,
    val totalDownloadSize : Int = 0,
    val downloadsCount : Int = 0
) {
}