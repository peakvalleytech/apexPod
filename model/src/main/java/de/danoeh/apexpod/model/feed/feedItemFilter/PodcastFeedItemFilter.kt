package de.danoeh.apexpod.model.feed.feedItemFilter

import android.util.Log
import de.danoeh.apexpod.model.feed.FeedItem

class PodcastFeedItemFilter(val feedIds : Set<Long>) : FeedItemFilter2 {
    private val TAG = "PodcastFeedItemFilter"
    override fun filter(items: List<FeedItem>) : List<FeedItem> {
        Log.d(TAG,"Filtering feed items by podcasts");
        
        return items.filter { feedItem -> feedIds.contains(feedItem.feedId) };
    }
}