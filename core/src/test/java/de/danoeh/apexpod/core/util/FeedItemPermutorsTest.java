package de.danoeh.apexpod.core.util;

import de.danoeh.apexpod.model.feed.FeedPreferences;
import de.danoeh.apexpod.model.feed.SortOrder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.model.feed.VolumeAdaptionSetting;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for FeedItemPermutors.
 */
public class FeedItemPermutorsTest {

    @Test
    public void testEnsureNonNullPermutors() {
        for (SortOrder sortOrder : SortOrder.values()) {
            assertNotNull("The permutor for SortOrder " + sortOrder + " is unexpectedly null",
                    FeedItemPermutors.getPermutor(sortOrder));
        }
    }

    @Test
    public void testPermutorForRule_EPISODE_TITLE_ASC() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.EPISODE_TITLE_A_Z);

        List<FeedItem> itemList = getTestList();
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 1, 2, 3)); // after sorting
    }

    @Test
    public void testPermutorForRule_EPISODE_TITLE_ASC_NullTitle() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.EPISODE_TITLE_A_Z);

        List<FeedItem> itemList = getTestList();
        itemList.get(2) // itemId 2
                .setTitle(null);
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 2, 1, 3)); // after sorting
    }


    @Test
    public void testPermutorForRule_EPISODE_TITLE_DESC() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.EPISODE_TITLE_Z_A);

        List<FeedItem> itemList = getTestList();
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 3, 2, 1)); // after sorting
    }

    @Test
    public void testPermutorForRule_DATE_ASC() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.DATE_OLD_NEW);

        List<FeedItem> itemList = getTestList();
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 1, 2, 3)); // after sorting
    }

    @Test
    public void testPermutorForRule_DATE_ASC_NulPubDatel() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.DATE_OLD_NEW);

        List<FeedItem> itemList = getTestList();
        itemList.get(2) // itemId 2
                .setPubDate(null);
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 2, 1, 3)); // after sorting
    }

    @Test
    public void testPermutorForRule_DATE_DESC() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.DATE_NEW_OLD);

        List<FeedItem> itemList = getTestList();
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 3, 2, 1)); // after sorting
    }

    @Test
    public void testPermutorForRule_DURATION_ASC() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.DURATION_SHORT_LONG);

        List<FeedItem> itemList = getTestList();
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 1, 2, 3)); // after sorting
    }

    @Test
    public void testPermutorForRule_DURATION_DESC() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.DURATION_LONG_SHORT);

        List<FeedItem> itemList = getTestList();
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 3, 2, 1)); // after sorting
    }

    @Test
    public void testPermutorForRule_DURATION_DESC_NullMedia() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.DURATION_LONG_SHORT);

        List<FeedItem> itemList = getTestList();
        itemList.get(1) // itemId 3
                .setMedia(null);
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 2, 1, 3)); // after sorting
    }

    @Test
    public void testPermutorForRule_FEED_TITLE_ASC() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.FEED_TITLE_A_Z);

        List<FeedItem> itemList = getTestList();
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 1, 2, 3)); // after sorting
    }

    @Test
    public void testPermutorForRule_FEED_TITLE_DESC() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.FEED_TITLE_Z_A);

        List<FeedItem> itemList = getTestList();
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 3, 2, 1)); // after sorting
    }

    @Test
    public void testPermutorForRule_FEED_TITLE_DESC_NullTitle() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.FEED_TITLE_Z_A);

        List<FeedItem> itemList = getTestList();
        itemList.get(1) // itemId 3
            .getFeed().setTitle(null);
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 2, 1, 3)); // after sorting
    }

    @Test
    public void testPermutorForRule_FEED_PRIORITY() {
        Permutor<FeedItem> permutor = FeedItemPermutors.getPermutor(SortOrder.FEED_PRIORITY);

        List<FeedItem> itemList = getTestList();
        FeedPreferences item1Pref =
                new FeedPreferences(1, false, FeedPreferences.AutoDeleteAction.GLOBAL, VolumeAdaptionSetting.OFF, null, null);
        item1Pref.setPriority(2);

        FeedPreferences item2Pref =
                new FeedPreferences(2, false, FeedPreferences.AutoDeleteAction.GLOBAL, VolumeAdaptionSetting.OFF, null, null);
        item2Pref.setPriority(3);
        FeedPreferences item3Pref =
                new FeedPreferences(3, false, FeedPreferences.AutoDeleteAction.GLOBAL, VolumeAdaptionSetting.OFF, null, null);
        item3Pref.setPriority(1);
        itemList.get(0).getFeed().setPreferences(item1Pref); // item 1
        itemList.get(1).getFeed().setPreferences(item3Pref); // item 3
        itemList.get(2).getFeed().setPreferences(item2Pref); // item 2
        assertTrue(checkIdOrder(itemList, 1, 3, 2)); // before sorting
        permutor.reorder(itemList);
        assertTrue(checkIdOrder(itemList, 3, 1, 2)); // after sorting
    }
    /**
     * Generates a list with test data.
     */
    private List<FeedItem> getTestList() {
        List<FeedItem> itemList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, 0, 1);  // January 1st
        Feed feed1 = new Feed(null, null, "Feed title 1");
        FeedItem feedItem1 = new FeedItem(1, "Title 1", null, null, calendar.getTime(), 0, feed1);
        FeedMedia feedMedia1 = new FeedMedia(0, feedItem1, 1000, 0, 0, null, null, null, true, null, 0, 0);
        feedItem1.setMedia(feedMedia1);
        itemList.add(feedItem1);

        calendar.set(2019, 2, 1);  // March 1st
        Feed feed2 = new Feed(null, null, "Feed title 3");
        FeedItem feedItem2 = new FeedItem(3, "Title 3", null, null, calendar.getTime(), 0, feed2);
        FeedMedia feedMedia2 = new FeedMedia(0, feedItem2, 3000, 0, 0, null, null, null, true, null, 0, 0);
        feedItem2.setMedia(feedMedia2);
        itemList.add(feedItem2);

        calendar.set(2019, 1, 1);  // February 1st
        Feed feed3 = new Feed(null, null, "Feed title 2");
        FeedItem feedItem3 = new FeedItem(2, "Title 2", null, null, calendar.getTime(), 0, feed3);
        FeedMedia feedMedia3 = new FeedMedia(0, feedItem3, 2000, 0, 0, null, null, null, true, null, 0, 0);
        feedItem3.setMedia(feedMedia3);
        itemList.add(feedItem3);

        return itemList;
    }

    /**
     * Checks if both lists have the same size and the same ID order.
     *
     * @param itemList Item list.
     * @param ids      List of IDs.
     * @return <code>true</code> if both lists have the same size and the same ID order.
     */
    private boolean checkIdOrder(List<FeedItem> itemList, long... ids) {
        if (itemList.size() != ids.length) {
            return false;
        }

        for (int i = 0; i < ids.length; i++) {
            if (itemList.get(i).getId() != ids[i]) {
                return false;
            }
        }
        return true;
    }
}
