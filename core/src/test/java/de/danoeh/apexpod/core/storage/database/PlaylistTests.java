package de.danoeh.apexpod.core.storage.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.storage.ApexDBAdapter;
import de.danoeh.apexpod.model.Playlist;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static de.danoeh.apexpod.core.storage.DbTestUtils.saveFeedlist;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class PlaylistTests {
    private ApexDBAdapter adapter;
    private PlayListItemDao playListItemDao;
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
    public void getPlaylistsEmpty() {
        List<Playlist> playlists = adapter.getAllPlaylist();
        assertEquals(0, playlists.size());
    }

    @Test
    public void getPlaylistsNonEmpty() {
        Playlist playlist = new Playlist("Playlist 1");
        adapter.createPlaylist(playlist);
        List<Playlist> playlists = adapter.getAllPlaylist();
        assertEquals(1, playlists.size());
        assertEquals(playlist.getName(), playlists.get(0).getName());
    }

    @Test
    public void createMultiplePlaylists() {
        Playlist playlist1 = new Playlist("Playlist 1");
        adapter.createPlaylist(playlist1);
        Playlist playlist2 = new Playlist("Playlist 2");
        adapter.createPlaylist(playlist2);
        List<Playlist> playlists = adapter.getAllPlaylist();
        assertEquals(2, playlists.size());
        assertEquals(playlist1.getName(), playlists.get(0).getName());
        assertEquals(playlist2.getName(), playlists.get(1).getName());
        assertEquals(1, playlists.get(0).getId());
        assertEquals(2, playlists.get(1).getId());
    }

    @Test
    public void deletePlaylist() {
        Playlist playlist = new Playlist("Playlist 1");
        long playlistId = adapter.createPlaylist(playlist);
        List<FeedItem> playlistItems = createFeedItems();
        playListItemDao.addItemsByPlayistId(playlistId, playlistItems);
        int rowsAffected = adapter.deletePlaylist(playlistId);
        assertEquals(1, rowsAffected);
        List<Playlist> playlists = adapter.getAllPlaylist();
        assertEquals(0, playlists.size());
    }

    @Test
    public void addToPlaylist() {

    }

    @Test
    public void getAllByFeedItemId_should_return_no_playlist() {
        List<FeedItem> allItems = createFeedItems();
        Playlist playlist = new Playlist("PlayList 1");
        adapter.open();
        adapter.createPlaylist(playlist);
        List<Playlist> playlists = adapter.getAllPlaylist();
        playlist = playlists.get(0);
        List<Playlist> playlistsWithFeedItem = adapter.getPlaylListsByFeedId(playlist.getId());
        assertEquals(0, playlistsWithFeedItem.size());
    }

    @Test
    public void getAllByFeedItemId_when_items_appear_in_one_lists() {
        List<FeedItem> allItems = createFeedItems();
        Playlist playlist = new Playlist("PlayList 1");
        adapter.createPlaylist(playlist);
        List<Playlist> playlists = adapter.getAllPlaylist();
        playlist = playlists.get(0);
        playListItemDao.addItemsByPlayistId(playlist.getId(), allItems);
        for (FeedItem feedItem : allItems) {
            List<Playlist> playlistsWithFeedItem = adapter.getPlaylListsByFeedId(feedItem.getId());
            assertEquals(1, playlistsWithFeedItem.size());
            for (Playlist pI : playlistsWithFeedItem) {
                assertTrue(playlists.contains(pI));
            }
        }
    }

    @NonNull
    private List<FeedItem> createFeedItems() {
        List<Feed> feeds = saveFeedlist(5, 1, false);
        List<FeedItem> allItems = new ArrayList<>();
        for (Feed f : feeds) {
            allItems.addAll(f.getItems());
        }
        return allItems;
    }

    @Test
    public void getAllByFeedItemId_when_items_appear_in_multiple_lists() {
        List<FeedItem> allItems = createFeedItems();
        Playlist playList1 = new Playlist("PlayList 1");
        Playlist playlist2 = new Playlist("PlayList 2");
        adapter.createPlaylist(playList1);
        adapter.createPlaylist(playlist2);
        List<Playlist> playLists = adapter.getAllPlaylist();
        playList1 = playLists.get(0);
        playlist2 = playLists.get(1);
        playListItemDao.addItemsByPlayistId(playList1.getId(), allItems);
        playListItemDao.addItemsByPlayistId(playlist2.getId(), allItems);
        for (FeedItem feedItem : allItems) {
            List<Playlist> playlistsWithFeedItem = adapter.getPlaylListsByFeedId(feedItem.getId());
            assertEquals(2, playlistsWithFeedItem.size());
            for (Playlist pI : playlistsWithFeedItem) {
                assertTrue(playLists.contains(pI));
            }
        }
    }




}
