package de.danoeh.apexpod.core.storage.database;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static de.danoeh.apexpod.core.storage.DbTestUtils.saveFeedlist;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.storage.ApexDBAdapter;
import de.danoeh.apexpod.core.storage.PodDBAdapter;
import de.danoeh.apexpod.model.Playlist;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedItem;

@RunWith(RobolectricTestRunner.class)
public class PlayListItemDaoTest {
    private ApexDBAdapter adapter;
    PlayListItemDao playListItemDao;
    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        UserPreferences.init(context);

        ApexDBAdapter.init(context);
        ApexDBAdapter.deleteDatabase();
        adapter = ApexDBAdapter.getInstance();
        adapter.open();
        playListItemDao = new PlayListItemDao();

    }
    @Test
    public void addItems() {
        Playlist playlist = new Playlist("Playlist 1");
        adapter.createPlaylist(playlist);
        playlist = adapter.getAllPlaylist().get(0);
        List<FeedItem> feedItems = createFeedItems(5);
        playListItemDao.addItemsByPlayistId(playlist.getId(), feedItems);
        List<FeedItem> playlistItems = playListItemDao.getItemsByPlayListId(playlist.getId());
        assertEquals(feedItems.size(), playlistItems.size());
        for (int i = 0; i < playlistItems.size(); i++) {
            assertTrue(playlistItems.get(i).getId() != 0);
            assertEquals(playlistItems.get(i).getId(), playlistItems.get(i).getId());
        }
    }

    @Test
    public void getItemOfEmptyPlaylist() {
        List<FeedItem> items = playListItemDao.getItemsByPlayListId(1);
        assertEquals(0, items.size());
    }

    @Test
    public void getItemsOfNonEmptyPlaylist() {
        Playlist playlist = new Playlist("Playlist 1");
        playlist.setId(1);
        List<FeedItem> feedItems = createFeedItems(5);

        playListItemDao.addItemsByPlayistId(playlist.getId(), feedItems);
        List<FeedItem> playlistItems = playListItemDao.getItemsByPlayListId(playlist.getId());
        assertEquals(feedItems.size(), playlistItems.size());
        for (int i = 0; i < playlistItems.size(); i++) {
            assertTrue(playlistItems.get(i).getId() != 0);
            assertEquals(playlistItems.get(i).getId(), playlistItems.get(i).getId());
        }

    }

    @Test
    public void getItemsOfNonExistentPlaylistShouldReturnNull() {

    }



    @Test
    public void deleteItems() {
        Playlist playlist = new Playlist("Playlist 1");
        long playlistId = adapter.createPlaylist(playlist);
        List<FeedItem> feedItems = createFeedItems(5);
        playListItemDao.addItemsByPlayistId(playlistId, feedItems);
        for (FeedItem item : feedItems) {
            playListItemDao.deleteItemByPlayListId(playlistId, item);
        }
        List<FeedItem> playlistItems = playListItemDao.getItemsByPlayListId(playlistId);
        assertEquals(0, playlistItems.size());
    }

    private List<FeedItem> createFeedItems(int numItems) {
        if (numItems <= 0) {
            throw new IllegalArgumentException("numItems<=0");
        }

        List<Feed> feeds = saveFeedlist(numItems, numItems, false);
        List<FeedItem> allItems = new ArrayList<>();
        for (Feed f : feeds) {
            allItems.addAll(f.getItems());
        }

        return createPlaylistFromFeeds(numItems, allItems);
    }

    @NonNull
    private List<FeedItem> createPlaylistFromFeeds(int numItems, List<FeedItem> allItems) {
        Random random = new Random();
        List<FeedItem> playlist = new ArrayList<>();
        while (playlist.size() < numItems) {
            int index = random.nextInt(numItems);
            if (!playlist.contains(allItems.get(index))) {
                playlist.add(allItems.get(index));
            }
        }
        return playlist;
    }
}
