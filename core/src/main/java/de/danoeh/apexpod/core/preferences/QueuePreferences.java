package de.danoeh.apexpod.core.preferences;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.apexpod.model.feed.Feed;

public class QueuePreferences {

    private static String TAG = "UserPreferences";

    private static Context context;
    private static SharedPreferences prefs;

    private static String PREF_FILTER_FEEDS = "pref_filter_feeds";
    /**
     * Sets up the UserPreferences class.
     *
     * @throws IllegalArgumentException if context is null
     */
    public static void init(Context context) {
        Log.d(TAG, "Creating new instance of UserPreferences");
        QueuePreferences.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setFeedsFilter(Long... feedIds) {
        SharedPreferences.Editor editor = prefs.edit();
        String feedIdsString = "";

        for (Long feedIdIter : feedIds) {
            feedIdsString += "," + feedIdIter.toString();
        }

        editor.putString(PREF_FILTER_FEEDS, feedIdsString);
        editor.apply();
    }

    /**
     * Return the ids of the feeds to filter
     * @return the ids
     */
    public static List<Long> getFeedsFilter() {
        String feedIdsString = prefs.getString(PREF_FILTER_FEEDS, "");
        ArrayList<Long> feedIds = new ArrayList<>();

        if (feedIdsString.isEmpty()) {
            return feedIds;
        }

        for (String id : feedIdsString.split(",")) {
            if (!id.isEmpty()) {
                feedIds.add(Long.valueOf(id));
            }
        }

        return  feedIds;
    }

}
