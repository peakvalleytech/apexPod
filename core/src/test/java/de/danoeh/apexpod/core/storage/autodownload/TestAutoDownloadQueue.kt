package de.danoeh.apexpod.core.storage.autodownload

import de.danoeh.apexpod.core.util.FeedItemUtil
import de.danoeh.apexpod.model.feed.*
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*

class TestAutoDownloadQueue {
    private lateinit var items : MutableList<FeedItem>
    val feed = Feed("url", null, "title")
    lateinit var feedPreferences : FeedPreferences
    lateinit var feedItems : MutableList<FeedItem>
    val playedItem1 = FeedItem(
        0, "title",
        "played item 1", "link", Date(0), FeedItem.PLAYED, feed
    )

    val oldItem1 = FeedItem(
        1, "title",
        "item1", "link", Date(1), FeedItem.UNPLAYED, feed
    )
    val oldItem2 = FeedItem(
        2, "title",
        "item2", "link", Date(2), FeedItem.UNPLAYED, feed
    )
    val newItem1 = FeedItem(
        3, "title",
        "item1", "link", Date(3), FeedItem.NEW, feed
    )

    val newItem2 = FeedItem(
        4, "title",
        "item2", "link", Date(4), FeedItem.NEW, feed
    )

    val playedItem2 = FeedItem(
        5, "title",
        "played item 2", "link", Date(5), FeedItem.PLAYED, feed
    )
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
        feedItems = mutableListOf(playedItem1, newItem1, newItem2, oldItem1, oldItem2, playedItem2)

    }

    @Test
    fun `should get newest unplayed`() {
        val autoDownloadQueue = AutoDownloadQueue(feedItems)
        val unplayedItems = autoDownloadQueue.getUnplayedItems()
        unplayedItems.sortByNewest(false)
        val feedItems = unplayedItems.subList(0, 1)
        assertEquals(1, feedItems.size())
        assertEquals(4, feedItems.items.get(0).id)
    }

    @Test
    fun `should get oldest unplayed`() {
        val autoDownloadQueue = AutoDownloadQueue(feedItems)
        val unplayedItems = autoDownloadQueue.getUnplayedItems()
        unplayedItems.sortByNewest(true)
        val feedItems = unplayedItems.subList(0, 1)
        assertEquals(1, feedItems.size())
        assertEquals(1, feedItems.items.get(0).id)
    }

    @Test
    fun `GIVEN items should download next unplayed items`() {
        // TODO: 12/10/2021 Test is broken
//        newItem1.media = FeedMedia(newItem1, "url", 1000, "mp3")
//        val autoDownloadQueue = AutoDownloadQueue(feedItems)
//        val nextDownloads = autoDownloadQueue.getNextDownloads(1)
//        assertEquals(1, nextDownloads.size())
//        assertEquals(1, nextDownloads.toList().get(0).id)
    }

    @Test
    fun `GIVEN non existent unplayed items THEN return empty list`() {
        feedItems = mutableListOf(playedItem1, playedItem2)
        val autoDownloadQueue = AutoDownloadQueue(feedItems)
        val nextDownloads = autoDownloadQueue.getNextDownloads(1)
        assertEquals(nextDownloads.size(), 0)
    }
}