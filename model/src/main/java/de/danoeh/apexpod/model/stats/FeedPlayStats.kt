package de.danoeh.apexpod.model.stats

import de.danoeh.apexpod.model.feed.Feed

class FeedPlayStats(
    val items : List<FeedPlayStatsItem>
) {
    fun calculateTotalListeningTime() : Long? {
        return items.fold(0) {acc, feedPlayStatsItem ->
            acc?.plus(feedPlayStatsItem.totalListeningTime)
        }
    }

    fun calculateTotalSpeedAdjustedListeningTime() : Long? {
        return items.fold(0) {acc, feedPlayStatsItem ->
            acc?.plus(feedPlayStatsItem.totalSpeedAdjustedListeningTime)
        }
    }
}