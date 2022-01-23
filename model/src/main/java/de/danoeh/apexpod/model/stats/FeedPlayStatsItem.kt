package de.danoeh.apexpod.model.stats

import de.danoeh.apexpod.model.feed.Feed

class FeedPlayStatsItem(
    val feed : Feed?,
    val totalSpeedAdjustedListeningTime : Long,
    val totalListeningTime : Long,
    val episodesListenedTo : Int
) {

}