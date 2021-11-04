package de.danoeh.apexpod.core.storage.repository;

import de.danoeh.apexpod.model.Playlist;

public interface PlaylistRepository {
    public void addPlaylist(Playlist playlist);

    public void updatePlaylist(Playlist playlist);

    public void deletePlaylist(long id);
}
