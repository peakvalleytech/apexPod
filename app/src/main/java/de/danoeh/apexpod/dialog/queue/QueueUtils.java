package de.danoeh.apexpod.dialog.queue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedItem;

public class QueueUtils {
    public static List<Feed> getFeedsFromFeedItems(List<FeedItem> feedItems) {
        Set<Feed> feedIds = new HashSet<>();
        List<Feed> feeds = new ArrayList<>();
        for (FeedItem feedItem : feedItems) {
            if (!feedIds.contains(feedItem.getFeedId())) {
                feeds.add(feedItem.getFeed());
            }
        }
        return feeds;
    }
}
