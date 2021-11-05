package de.danoeh.apexpod.core.storage.repository;

import java.util.List;

import de.danoeh.apexpod.model.feed.FeedItem;

public interface PlayListItemRepository {
    void addItemsToPlayList(long id);

    List<FeedItem> getItemsByPlayListId(long id);

    void deleteItemsFromPlaylist(long id);
}
