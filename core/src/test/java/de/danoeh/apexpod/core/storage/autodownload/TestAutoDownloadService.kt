package de.danoeh.apexpod.core.storage.autodownload

import de.danoeh.apexpod.core.storage.autodownload.impl.AutoDownloadServiceImpl
import de.danoeh.apexpod.model.feed.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class TestAutoDownloadService {
    private lateinit var items : MutableList<FeedItem>
    val feed = Feed("url", null, "title")
    lateinit var feedPreferences : FeedPreferences
    lateinit var feedItems : List<FeedItem>
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
        feedItems = listOf(newItem1, newItem2, oldItem1, oldItem2)

    }

    /**
     *
     */
    @Test
    fun `when limit is lt number of new items select up to limit`() {
        feedPreferences.autoDownloadPreferences = AutoDownload(1, true, false)

        val selectedFeedItems = AutoDownloadServiceImpl.selectFeedItems(feedPreferences.autoDownloadPreferences,
            feedItems
            )
        assertEquals(1, selectedFeedItems.size)
        val expectedItemId = 2L
        val actualItemId = selectedFeedItems.get(0).id
        assertEquals(expectedItemId, actualItemId)
    }

    @Test
    fun `when limit gt number of new items select all new and unplayed items`() {
        feedPreferences.autoDownloadPreferences = AutoDownload(4, true, false)
        val selectedFeedItems = AutoDownloadServiceImpl.selectFeedItems(feedPreferences.autoDownloadPreferences,
            feedItems
        )
        assertEquals(2, selectedFeedItems.size)
        selectedFeedItems.forEach {
            assertTrue(it.playState == FeedItem.NEW)
        }
    }
    @Test
    fun `when limit is lt number of unplayed items select up to limit`() {
        feedPreferences.autoDownloadPreferences = AutoDownload(4, true, true)
        val selectedFeedItems = AutoDownloadServiceImpl.selectFeedItems(feedPreferences.autoDownloadPreferences,
            feedItems
        )
        assertEquals(4, selectedFeedItems.size)
    }

    @Test
    fun `should sort items from new to old`() {
        feedPreferences.autoDownloadPreferences = AutoDownload(feedItems.size, true, true)
        val selectedFeedItems = AutoDownloadServiceImpl.selectFeedItems(feedPreferences.autoDownloadPreferences,
            feedItems
        )
        assertEquals(feedItems.reversed(), selectedFeedItems)
    }

    @Test
    fun `should sort items from old to new`() {
        feedPreferences.autoDownloadPreferences = AutoDownload(feedItems.size, false, true)
        val selectedFeedItems = AutoDownloadServiceImpl.selectFeedItems(feedPreferences.autoDownloadPreferences,
            feedItems
        )
        assertEquals(feedItems, selectedFeedItems)
    }
}