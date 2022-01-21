package de.danoeh.apexpod.core.storage.database;


import static org.junit.Assert.assertEquals;
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
import de.danoeh.apexpod.model.Playlist;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.stats.PlayStat;
import de.danoeh.apexpod.model.stats.PlayStatRange;

@RunWith(RobolectricTestRunner.class)
public class PlayStatDaoTest {
    private ApexDBAdapter adapter;
    PlayStatDao playStatsDao;

    class TestData {

        PlayStatRange emptyList = new PlayStatRange(0, 0);
        PlayStatRange singleFeedList = new PlayStatRange(0, 100);
        PlayStatRange multiFeedList = new PlayStatRange(0, 100);
        public TestData() {
            long feedItemId = 1;
            long feedId = 1;
            singleFeedList.add(new PlayStat(0, feedItemId, feedId,0, 1, 0, 0));
            singleFeedList.add(new PlayStat(0, feedItemId, feedId,2, 4, 0, 0));
            singleFeedList.add(new PlayStat(0, feedItemId, feedId,5, 8, 0, 0));
            singleFeedList.add(new PlayStat(0, feedItemId, feedId,9, 13, 0, 0));
            singleFeedList.add(new PlayStat(0, feedItemId, feedId,14, 19, 0, 0));

        }

    }
    private TestData data;
    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        UserPreferences.init(context);
        ApexDBAdapter.init(context);
        ApexDBAdapter.deleteDatabase();
        ApexDBAdapter.tearDownTests();
        adapter = ApexDBAdapter.getInstance();
        adapter.open();
        playStatsDao = new PlayStatDao();
        data = new TestData();
    }
    @Test
    public void givenPlayStats_whenValid_shouldAdded() {
        List<PlayStat> playStats = new ArrayList<>();
        int numOfStats = data.singleFeedList.size();
        int feedItemId = 4;
        for (int i = 0; i < numOfStats; ++i) {
            playStats.add(data.singleFeedList.get(i));
        }
        for (int i = 0; i < numOfStats; ++i) {
            long expectedId = playStatsDao.createPlayStat(playStats.get(i));
            assertEquals(  i + 1, expectedId);
        }
    }

    @Test
    public void givenTableNotEmpty_whenGettingAllItems_shouldReturnAll() {
        List<PlayStat> playStats = new ArrayList<>();
        int numOfStats = data.singleFeedList.size();
        for (int i = 0; i < numOfStats; ++i) {
            playStats.add(data.singleFeedList.get(i));
        }
        for (int i = 0; i < numOfStats; ++i) {
            playStatsDao.createPlayStat(playStats.get(i));
        }
        PlayStatRange createdPlayStats = playStatsDao.getAllPlayStats();
        assertEquals(createdPlayStats.size(), playStats.size());
    }

    @Test
    public void givenFeedItemId_whenValid_shouldReturnMatchingPlayStats() {

    }

    @Test
    public void givenPlayStats_whenGettingByRange_shouldReturnMatchingPlayStats() {

    }

    @Test
    public void givenPlayStat_whenUpdated_shouldUpdatePlayStat() {

    }

    @Test
    public void givenPlayStat_whenDeleted_shouldDeletePlayStat() {

    }
}
