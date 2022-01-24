package de.danoeh.apexpod.core.storage.database

import de.danoeh.apexpod.model.stats.PlayStat
import de.danoeh.apexpod.model.stats.PlayStatRange
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.Exception
import java.util.ArrayList

class PlayStatRangeTest {
    internal class TestData {
        var emptyList = PlayStatRange()
        var singleFeedList = PlayStatRange()
        var multiFeedList = PlayStatRange()
        private fun initSingleFeedList() {
            val feedItemId: Long = 1
            val feedId: Long = 1
            singleFeedList.add(PlayStat(0, feedItemId, feedId, 0, 1, 0, 0))
            singleFeedList.add(PlayStat(0, feedItemId, feedId, 2, 4, 0, 0))
            singleFeedList.add(PlayStat(0, feedItemId, feedId, 5, 8, 0, 0))
            singleFeedList.add(PlayStat(0, feedItemId, feedId, 9, 13, 0, 0))
            singleFeedList.add(PlayStat(0, feedItemId, feedId, 14, 19, 0, 0))
        }

        private fun initMultiFeedList() {
            val feedItemId: Long = 1
            multiFeedList.add(PlayStat(0, feedItemId, 1, 0, 1, 0, 10))
            multiFeedList.add(PlayStat(0, feedItemId, 2, 2, 4, 10, 20))
            multiFeedList.add(PlayStat(0, feedItemId, 3, 5, 8, 20, 30))
            multiFeedList.add(PlayStat(0, feedItemId, 4, 9, 13, 30, 40))
            multiFeedList.add(PlayStat(0, feedItemId, 5, 14, 19, 5, 10))
        }

        init {
            initSingleFeedList()
            initMultiFeedList()
        }
    }

    private var data: TestData? = null

    @Before
    fun setUp() {
        data = TestData()
    }

    @Test
    fun givenStartEqEnd_whenCreatingInstance_returnEmptyRange() {
        var playStatRange = PlayStatRange()
        var expectedSize = 0
        var actualSize = playStatRange.size()
        assertEquals(expectedSize, actualSize)
    }

    @Test
    fun shouldReturnTotalTime() {
        val expectedTotalTime = 15L
        val actualTotalTime = data?.multiFeedList?.getTotalTime()
        assertEquals(expectedTotalTime, actualTotalTime)
    }

    @Test
    fun shouldReturnTotalDuration() {
        val expectedTotalDuration = 45L
        val actualTotalDuration = data?.multiFeedList?.getTotalDuration()
        assertEquals(expectedTotalDuration, actualTotalDuration)
    }
}