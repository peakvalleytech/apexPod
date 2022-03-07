package de.test.antennapod.service.playback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.danoeh.apexpod.core.preferences.PlaybackPreferences;
import de.danoeh.apexpod.core.preferences.SleepTimerPreferences;
import de.danoeh.apexpod.core.service.playback.PlaybackServiceTaskManager;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.PodDBAdapter;
import de.danoeh.apexpod.core.storage.database.PlayListItemDao;
import de.danoeh.apexpod.core.storage.repository.impl.PlaylistRepositoryImpl;
import de.danoeh.apexpod.core.widget.WidgetUpdater;
import de.danoeh.apexpod.model.Playlist;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.playback.Playable;

public class ContinousPlaybackTest {
    @After
    public void tearDown() {
        PodDBAdapter.deleteDatabase();
    }

    private Context context;
    private List<FeedItem> queue = null;
    private List<FeedItem> playlistItems = null;
    private List<FeedItem> feedItems = null;

    @Before
    public void setUp() {
        // create new database
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        PodDBAdapter.init(context);
        PodDBAdapter.deleteDatabase();
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.close();
        SleepTimerPreferences.setShakeToReset(false);
        SleepTimerPreferences.setVibrate(false);
        PlaybackPreferences.init(context);
        queue = writeTestQueue("a");
        playlistItems = writeTestPlaylist("b");
        feedItems = writeTestFeed("c");
    }

    @Test
    public void testGetQueue() throws InterruptedException {
        PlaybackPreferences.setCurrentAutoPlayPlaylist(PlaybackPreferences.AUTOPLAY_QUEUE);
        PlaybackServiceTaskManager pstm = new PlaybackServiceTaskManager(context, defaultPSTM);

        List<FeedItem> testQueue = pstm.getQueue();
        assertNotNull(testQueue);
        assertEquals(testQueue.size(), queue.size());
        for (int i = 0; i < queue.size(); i++) {
            assertEquals(testQueue.get(i).getId(), queue.get(i).getId());
        }
        pstm.shutdown();
    }
    @Test
    public void testGetPlayList() throws InterruptedException {
        PlaybackPreferences.setCurrentAutoPlayPlaylist(PlaybackPreferences.AUTOPLAY_PLAYLIST);
        PlaybackServiceTaskManager pstm = new PlaybackServiceTaskManager(context, defaultPSTM);
        PlaybackPreferences.setCurrentAutoPlayPlaylistId(1);
        List<FeedItem> testQueue = pstm.getQueue();
        assertNotNull(testQueue);
        assertEquals(testQueue.size(), playlistItems.size());
        for (int i = 0; i < playlistItems.size(); i++) {
            assertEquals(testQueue.get(i).getId(), playlistItems.get(i).getId());
        }
        pstm.shutdown();
    }

    @Test
    public void switchingBetweenQueueAndPlaylist_shouldSwitchAutoPlayMode() throws InterruptedException {
        for (int i = 0; i < 10; ++i) {
           testGetQueue();
           testGetPlayList();
        }
    }

    private List<FeedItem> writeTestQueue(String pref) {
        final int NUM_ITEMS = 10;
        Feed f = new Feed();
        f.setTitle("Title");
        f.setItems(new ArrayList<>());
        for (int i = 0; i < NUM_ITEMS; i++) {
            f.getItems().add(new FeedItem(0, pref + i, pref + i, "link", new Date(), FeedItem.PLAYED, f));
        }
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.setCompleteFeed(f);
        adapter.setQueue(f.getItems());
        adapter.close();

        for (FeedItem item : f.getItems()) {
            assertTrue(item.getId() != 0);
        }
        return f.getItems();
    }

    private List<FeedItem> writeTestPlaylist(String pref) {
        final int NUM_ITEMS = 4;
        Feed f = new Feed();
        f.setTitle("testFeed");
        f.setItems(new ArrayList<>());
        for (int i = 0; i < NUM_ITEMS; i++) {
            f.getItems().add(new FeedItem(0, pref + i, pref + i, "link", new Date(), FeedItem.PLAYED, f));
        }

        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.setCompleteFeed(f);
        adapter.close();
        f.setId(1);
        List<Feed> feeds = DBReader.getFeedList();
        Feed feed = null;
        for (Feed fItr : feeds) {
            if (fItr.getTitle().equals(f.getTitle())) {
                feed = fItr;
            }
        }
        List<FeedItem> feedItems = DBReader.getFeedItemList(feed);
        assertEquals(NUM_ITEMS, feedItems.size());
        PlaylistRepositoryImpl playlistRepository = new PlaylistRepositoryImpl(context);
        Playlist playlist = new Playlist("Playlist 1");
        playlistRepository.addPlaylist(playlist);

        PlayListItemDao playListItemDao = new PlayListItemDao();
        playListItemDao.addItemsByPlayistId(1, feedItems);
        List<FeedItem> playlistItems = playListItemDao.getItemsByPlayListId(1);

        assertEquals(NUM_ITEMS, playlistItems.size());

        return playlistItems;
    }

    private List<FeedItem> writeTestFeed(String pref) {
        final int NUM_ITEMS = 5;
        Feed f = new Feed(0, null, "title", "link", "d", null, null, null, null, "id", null, "null", "url", false);
        f.setItems(new ArrayList<>());
        for (int i = 0; i < NUM_ITEMS; i++) {
            f.getItems().add(new FeedItem(0, pref + i, pref + i, "link", new Date(), FeedItem.PLAYED, f));
        }
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.setCompleteFeed(f);
        adapter.close();

        for (FeedItem item : f.getItems()) {
            assertTrue(item.getId() != 0);
        }

        return f.getItems();
    }

    private final PlaybackServiceTaskManager.TaskManagerCallback defaultPSTM = new PlaybackServiceTaskManager.TaskManagerCallback() {
        @Override
        public void positionSaverTick() { }

        @Override
        public void onSleepTimerAlmostExpired(long timeLeft) { }

        @Override
        public void onSleepTimerExpired() { }

        @Override
        public void onSleepTimerReset() { }

        @Override
        public WidgetUpdater.WidgetState requestWidgetState() {
            return null;
        }

        @Override
        public void onChapterLoaded(Playable media) { }
    };
}
