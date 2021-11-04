package de.danoeh.apexpod.model;

import java.util.List;

import de.danoeh.apexpod.model.feed.FeedItem;

public class Playlist {
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
}
