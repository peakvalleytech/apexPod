package de.danoeh.apexpod.model.stats

import de.danoeh.apexpod.model.feed.Feed

class FeedPlayStatsItem(
    val feed : Feed,
    val timePlayed : Long,
    val timePlayedSpeedAdjusted : Long,
    val totalPlayingTime : Long
) {

}