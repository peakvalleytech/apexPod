package de.danoeh.apexpod.core.storage;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.model.Playlist;

import static de.danoeh.apexpod.core.storage.DbTestUtils.saveFeedlist;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class PlaylistTests {
    private ApexDBAdapter adapter;
    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        UserPreferences.init(context);

        ApexDBAdapter.init(context);
        ApexDBAdapter.deleteDatabase();
        adapter = ApexDBAdapter.getInstance();
        adapter.open();
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
        adapter.createPlaylist(playlist);
        int rowsAffected = adapter.deletePlaylist(1);
        assertEquals(1, rowsAffected);
    }

    @Test
    public void addToPlaylist() {

    }
}
