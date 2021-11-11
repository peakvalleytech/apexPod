package de.danoeh.apexpod.core.storage.repository.impl;

import android.content.Context;

import java.util.List;

import de.danoeh.apexpod.core.storage.ApexDBAdapter;
import de.danoeh.apexpod.core.storage.repository.PlaylistRepository;
import de.danoeh.apexpod.model.Playlist;

public class PlaylistRepositoryImpl implements PlaylistRepository {
        ApexDBAdapter dbAdapter;

    public PlaylistRepositoryImpl(Context context) {
        ApexDBAdapter.init(context);
        dbAdapter = ApexDBAdapter.getInstance();
    }

    @Override
    public List<Playlist> getPlaylists() {
        return dbAdapter.getAllPlaylist();
    }

    public void addPlaylist(Playlist playlist) {
        dbAdapter.createPlaylist(playlist);
    }

    public void updatePlaylist(Playlist playlist) {
    }

    public void deletePlaylist(long id) {
        dbAdapter.deletePlaylist(id);
    }

    public List<Playlist> getPlayListsByFeedItemId(long id) {
        return dbAdapter.getPlaylListsByFeedId(id);
    }
}
