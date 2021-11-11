package de.danoeh.apexpod.core.storage.repository;

import java.util.List;

import de.danoeh.apexpod.model.Playlist;

public interface PlaylistRepository {
    List<Playlist> getPlaylists();

    public void addPlaylist(Playlist playlist);

    public void updatePlaylist(Playlist playlist);

    public void deletePlaylist(long id);

    public void getPlayListsByFeedItemId(long id);
}
