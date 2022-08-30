package de.danoeh.apexpod.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

import de.danoeh.apexpod.model.feed.FeedItem;

public class Playlist implements Serializable {
    private long id;
    private String name;

    public Playlist(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Playlist) {
            Playlist playlist = (Playlist) obj;
            return playlist.id == this.id && playlist.getName().equals(this.getName());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (int)this.id + this.name.hashCode();
    }
}
