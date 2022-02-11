package de.danoeh.apexpod.core.storage.database

import de.danoeh.apexpod.model.stats.FeedPlayStats
import de.danoeh.apexpod.model.stats.FeedPlayStatsItem
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FeedPlayStatsTest {
    private lateinit var feedPlayStats : FeedPlayStats

    @Before
    fun setup() {
        val feedPlayStatItems = mutableListOf<FeedPlayStatsItem>(
            FeedPlayStatsItem(null, 5, 10, 5),
            FeedPlayStatsItem(null, 5, 10, 5),
            FeedPlayStatsItem(null, 5, 10, 5),
            FeedPlayStatsItem(null, 5, 10, 5),
            FeedPlayStatsItem(null, 5, 10, 5),
        )
        feedPlayStats = FeedPlayStats(feedPlayStatItems)
    }

    @Test
    fun shouldCalculateTotalListeningTime() {
        val expectedTotalListeningTime = 50L
        val actualTotalListeningTime =
            feedPlayStats.calculateTotalListeningTime()
        assertEquals(expectedTotalListeningTime, actualTotalListeningTime)
    }

    @Test
    fun shouldCalculateTotalSpeedAdjustedListeningTime() {
        val expectedTotalSpeedAdjustedListeningTime = 25L
        val actualTotalSpeedAdjustedListeningTime =
            feedPlayStats.calculateTotalSpeedAdjustedListeningTime()
        assertEquals(expectedTotalSpeedAdjustedListeningTime, actualTotalSpeedAdjustedListeningTime)
    }
}