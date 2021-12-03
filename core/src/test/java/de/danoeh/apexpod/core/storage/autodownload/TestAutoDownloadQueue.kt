package de.danoeh.apexpod.core.storage.autodownload

import de.danoeh.apexpod.model.feed.Feed
import de.danoeh.apexpod.model.feed.FeedItem
import de.danoeh.apexpod.model.feed.FeedPreferences
import de.danoeh.apexpod.model.feed.VolumeAdaptionSetting
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class TestAutoDownloadQueue {
    private lateinit var items : MutableList<FeedItem>
    val feed = Feed("url", null, "title")
    lateinit var feedPreferences : FeedPreferences
    lateinit var feedItems : MutableList<FeedItem>
    @Before
    fun setup(){
        feedPreferences = FeedPreferences(
            1,
            true,
            FeedPreferences.AutoDeleteAction.GLOBAL,
            VolumeAdaptionSetting.OFF,
            "",
            ""
        )
        val newItem1 = FeedItem(
            1, "title",
            "item1", "link", Date(), FeedItem.NEW, feed
        )
        val newItem2 = FeedItem(
            2, "title",
            "item2", "link", Date(), FeedItem.NEW, feed
        )
        val oldItem1 = FeedItem(
            3, "title",
            "item1", "link", Date(), FeedItem.UNPLAYED, feed
        )
        val oldItem2 = FeedItem(
            4, "title",
            "item2", "link", Date(), FeedItem.UNPLAYED, feed
        )
        feedItems = mutableListOf(newItem1, newItem2, oldItem1, oldItem2)

    }

    @Test
    fun `should get new unplayed`() {
        val autoDownloadQueue = AutoDownloadQueue(feedItems)
        val newUnplayedItems = autoDownloadQueue.getUnplayedItems(false)
        assertEquals(2, newUnplayedItems.size())
    }

    @Test
    fun `should get all unplayed`() {
        val autoDownloadQueue = AutoDownloadQueue(feedItems)
        val newUnplayedItems = autoDownloadQueue.getUnplayedItems(true)
        assertEquals(4, newUnplayedItems.size())
    }
}