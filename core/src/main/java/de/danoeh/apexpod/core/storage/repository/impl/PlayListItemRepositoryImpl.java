package de.danoeh.apexpod.core.storage.repository.impl;

import java.util.List;

import de.danoeh.apexpod.core.storage.database.PlayListItemDao;
import de.danoeh.apexpod.core.storage.repository.PlayListItemRepository;
import de.danoeh.apexpod.model.feed.FeedItem;

public class PlayListItemRepositoryImpl implements PlayListItemRepository {
    private PlayListItemDao playListItemDao;
    public PlayListItemRepositoryImpl(PlayListItemDao playListItemDao) {
        this.playListItemDao = playListItemDao;
    }


    @Override
    public void addItemsToPlayList(long id) {

    }

    @Override
    public List<FeedItem> getItemsByPlayListId(long id) {
        return null;
    }

    @Override
    public void deleteItemsFromPlaylist(long id) {

    }
}
