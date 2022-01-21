package de.danoeh.apexpod.model.stats

import de.danoeh.apexpod.model.feed.Feed

class FeedPlayStats(
    val items : List<FeedPlayStatsItem>
) {
    fun calculateTotalListeningTime() : Long? {
        return null
    }

    fun calculateTotalSpeedAdjustedListeningTime() : Long? {
        return null
    }
}