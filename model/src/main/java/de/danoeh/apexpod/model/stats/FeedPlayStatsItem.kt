package de.danoeh.apexpod.model.stats

import de.danoeh.apexpod.model.feed.Feed

data class FeedPlayStatsItem(
    val feed : Feed?,
    val totalSpeedAdjustedListeningTime : Long = 0,
    val totalListeningTime : Long = 0,
    val episodesListenedTo : Int = 0
) {
}