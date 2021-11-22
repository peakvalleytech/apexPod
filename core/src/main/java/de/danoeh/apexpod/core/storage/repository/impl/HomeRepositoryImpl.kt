package de.danoeh.apexpod.core.storage.repository.impl

import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.core.storage.repository.HomeRepository
import de.danoeh.apexpod.model.feed.FeedItem
import kotlin.math.absoluteValue
import kotlin.random.Random

class HomeRepositoryImpl() : HomeRepository {
    override fun getFeaturedEpisode(): FeedItem? {
        val feeds = DBReader.getFeedList()
        if (feeds.size > 0) {
            val random = Random(System.currentTimeMillis())
            val feed = feeds.get(random.nextInt().absoluteValue % feeds.size)
            val feedItems = DBReader.getFeedItemList(feed)
            if (feedItems.size > 0) {
                val feedItem = feedItems.get(random.nextInt().absoluteValue % feedItems.size)
                return feedItem
            }
        }
        return null
    }
}