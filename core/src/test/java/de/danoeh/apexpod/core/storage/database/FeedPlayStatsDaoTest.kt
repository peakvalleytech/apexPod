package de.danoeh.apexpod.core.storage.database

import androidx.test.platform.app.InstrumentationRegistry
import de.danoeh.apexpod.core.preferences.UserPreferences
import de.danoeh.apexpod.core.storage.ApexDBAdapter
import de.danoeh.apexpod.model.feed.Feed
import de.danoeh.apexpod.model.stats.FeedPlayStatsItem
import de.danoeh.apexpod.model.stats.PlayStat
import de.danoeh.apexpod.model.stats.PlayStatRange
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FeedPlayStatsDaoTest {
    private var adapter: ApexDBAdapter? = null
    var feedPlayStatsDao: FeedPlayStatsDao? = null
    var playStatsDao : PlayStatDao? = null
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        UserPreferences.init(context)
        ApexDBAdapter.init(context)
        ApexDBAdapter.deleteDatabase()
        adapter = ApexDBAdapter.getInstance()
        adapter?.open()
        feedPlayStatsDao = FeedPlayStatsDao()
        playStatsDao = PlayStatDao()
        adapter?.close()
    }

    @Test
    fun shouldGetFeeds() {
        val feed1 =
            Feed(0, null, "A", "link", "d", null, null, null, "rss", "A", null, "", "", true)
        val feed2 =
            Feed(0, null, "b", "link", "d", null, null, null, "rss", "b", null, "", "", true)
        val feed3 =
            Feed(0, null, "C", "link", "d", null, null, null, "rss", "C", null, "", "", true)
        val feed4 =
            Feed(0, null, "d", "link", "d", null, null, null, "rss", "d", null, "", "", true)
        adapter!!.open()
        adapter!!.setCompleteFeed(feed1)
        adapter!!.setCompleteFeed(feed2)
        adapter!!.setCompleteFeed(feed3)
        adapter!!.setCompleteFeed(feed4)
        val feedPlayStats = feedPlayStatsDao?.getFeedPlayStats()
        adapter?.close()
        val expectedSize = 4
        val actualSize = feedPlayStats?.size()
        assertEquals(expectedSize, actualSize)
    }

    @Test
    fun shouldGetFeedPlayStatsItems() {
        val multiFeedList = PlayStatRange()
        val feedItemId: Long = 1
        val feed1 =
            Feed(0, null, "A", "link", "d", null, null, null, "rss", "A", null, "", "", true)
        adapter!!.open()
        adapter!!.setCompleteFeed(feed1)

        multiFeedList.add(PlayStat(0, feedItemId, 1, 0, 5, 0, 10))
        multiFeedList.add(PlayStat(0, feedItemId, 1, 0, 5, 0, 10))
        multiFeedList.add(PlayStat(0, feedItemId, 1, 0, 5, 0, 10))
        multiFeedList.add(PlayStat(0, feedItemId, 1, 0, 5, 0, 10))
        for (i in 0 until multiFeedList.size()) {
            playStatsDao?.createPlayStat(multiFeedList.get(i))
        }
        val expectedFeedPlayStatsItem =
            FeedPlayStatsItem(
                feed1,
                20,
                40,
                0
                )
        val feedPlayStats = feedPlayStatsDao?.getFeedPlayStats()
        adapter?.close()
        val feedPlayStatsItemList = feedPlayStats?.items
        assertEquals(1, feedPlayStatsItemList?.size)
        val actualFeedPlayStatsItem = feedPlayStatsItemList?.get(0)
        assertEquals(expectedFeedPlayStatsItem, actualFeedPlayStatsItem)
    }
}