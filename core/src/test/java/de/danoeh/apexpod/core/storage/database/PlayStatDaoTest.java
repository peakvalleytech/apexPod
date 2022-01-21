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
        PlayStatRange simpleList = new PlayStatRange(0, 100);
        PlayStatRange list = new PlayStatRange(0, 100);
        public TestData() {
            long feedItemId = 1;
            simpleList.add(new PlayStat(0, feedItemId, 0, 1, 0, 0));
            simpleList.add(new PlayStat(0, feedItemId, 2, 4, 0, 0));
            simpleList.add(new PlayStat(0, feedItemId, 5, 8, 0, 0));
            simpleList.add(new PlayStat(0, feedItemId, 9, 13, 0, 0));
            simpleList.add(new PlayStat(0, feedItemId, 14, 19, 0, 0));
        }

    }
    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        UserPreferences.init(context);
        ApexDBAdapter.init(context);
        ApexDBAdapter.deleteDatabase();
        adapter = ApexDBAdapter.getInstance();
        adapter.open();
        playStatsDao = new Pl;
    }
    @Test
    public void givenPlayStats_whenAdded_shouldSucceed() {
        List<PlayStat> playStats = new ArrayList<>();
        int numOfStats = 5;
        int feedItemId = 4;
        for (int i = 0; i < numOfStats; ++numOfStats) {
            playStats.add(new PlayStat(0, feedItemId, 1, 2, 3, 4));
        }
        for (int i = 0; i < numOfStats; ++numOfStats) {
            long id = playStatsDao.createPlayStat(playStats.get(i));
            assertTrue(id == i + 1);
        }
        PlayStatRange createdPlayStats = playStatsDao.getAllPlayStats();
        assertEquals(createdPlayStats.size(), playStats.size());
        for (int i = 0; i < createdPlayStats.size(); i++) {
            assertTrue(createdPlayStats.get(i).getId() != 0);
            assertEquals(createdPlayStats.get(i).getId(), playStats.get(i).getId());
        }
    }

    @Test
    public void givenPlayStats_whenGettingByFeedItemId_shouldReturnMatchingPlayStats() {

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
