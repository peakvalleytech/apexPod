package de.danoeh.apexpod.core.storage.database.autodelete

//import de.danoeh.apexpod.core.storage.autodelete.impl.rules.IncludeQueuedAutoDeleteRule
import de.danoeh.apexpod.core.storage.autodelete.impl.AutoDeleteFilterFactory
import de.danoeh.apexpod.model.feed.Feed
import de.danoeh.apexpod.model.feed.FeedItem
import de.danoeh.apexpod.model.feed.FeedMedia
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class TestAutoDeleteFilter {
    lateinit var autoDeleteFactory : AutoDeleteFilterFactory
    private var hrsAfterPlaybackValues = mutableListOf(0, 12, 24, 72, 120, 168)
    init {
        autoDeleteFactory = AutoDeleteFilterFactory()
    }

//    @Test
//    fun `given all episodes are unplayed should return empty list`() {
//        val autoDeleteFilter = autoDeleteFactory.createAutoDeleteFilter(0, false, false)
//        val feedItems = mutableListOf<FeedItem>()
//
//        autoDeleteFilter.filter(feedItems)
//
//    }

    @Test
    fun `should filter played items by hours after playback`() {
        val keepFavorite = false
        val feed = Feed("url", null, "title")
        val feedItems = mutableListOf<FeedItem>()

        feed.items = feedItems
        hrsAfterPlaybackValues.forEach {
            createItem(
                feed = feed,
                feedItems,
                itemState = FeedItem.PLAYED,
                hrsToPlaybackDate(it),
                false,
                keepFavorite
            )
        }

        var expectedSize = hrsAfterPlaybackValues.size
        hrsAfterPlaybackValues.forEach {
            val autoDeleteFilter = autoDeleteFactory.createAutoDeleteFilter(it, keepFavorite, false)
            val filteredItems = autoDeleteFilter.filter(feedItems)
            assertEquals(expectedSize, filteredItems.size)
            expectedSize--
        }
    }

    @Test
    fun `should return only non queued items`() {
        val hrsAfterPlayback = hrsAfterPlaybackValues.get(0)
        val keepFavorite = false
        val autoDeleteFilter = autoDeleteFactory.createAutoDeleteFilter(hrsAfterPlayback, keepFavorite, true)
        val feedItems = mutableListOf<FeedItem>()

        autoDeleteFilter.filter(feedItems)
        val feed = Feed("url", null, "title")
        feed.items = feedItems
        for (i in 1..5) {
            createItem(
                feed = feed,
                feedItems,
                itemState = FeedItem.PLAYED,
                hrsToPlaybackDate(hrsAfterPlayback),
                true,
                keepFavorite
                )
        }
        for (i in 1..5) {
            createItem(
                feed = feed,
                feedItems,
                itemState = FeedItem.PLAYED,
                hrsToPlaybackDate(hrsAfterPlayback),
                false,
                keepFavorite
            )
        }
        val filteredFeedItems = autoDeleteFilter.filter(feedItems)
        assertEquals(1, filteredFeedItems.size)
    }

    fun createItem(
        feed: Feed,
        items: MutableList<FeedItem>,
        itemState: Int,
        playbackCompletionDate: Date,
        addToQueue: Boolean,
        addToFavorites: Boolean
    ) {
        val item = FeedItem(0, "title", feed.title, "link", null, itemState, feed)
        item.setMedia(
            FeedMedia(
                0, item, 1, 0, 1L, "m",
                null, "url", true, playbackCompletionDate, 0, 0
            )
        )
        if (addToQueue)
            item.addTag(FeedItem.TAG_QUEUE)
        if (addToFavorites)
            item.addTag(FeedItem.TAG_FAVORITE)
        items.add(item)
    }

    /**
     * Given the hours after playback returns the playback completion date
     * Assumes that the current date is the value as returned by System.currentMillis()
     */
    fun hrsToPlaybackDate(hoursAfterPlayback : Int) : Date {
        var currentDate = Calendar.getInstance()
        currentDate.add(Calendar.HOUR, hoursAfterPlayback * -1)
        return  Date(currentDate.timeInMillis)
    }
}