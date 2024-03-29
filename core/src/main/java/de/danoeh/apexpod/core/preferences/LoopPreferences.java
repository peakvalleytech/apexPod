package de.danoeh.apexpod.core.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class LoopPreferences {
    private static Context context;
    private static SharedPreferences sharedPreferences;
    public static final String PREF_KEY_LOOP_ENABLED = "loop_enabled";
    public static final String PREF_KEY_LOOP_FEED_ID = "loop_feed_id";
    public static final String PREF_KEY_LOOP_START = "loop_start";
    public static final String PREF_KEY_LOOP_END = "loop_end";

    public static void init(Context context) {
        LoopPreferences.context = context.getApplicationContext();
        sharedPreferences = getSharedPreferences();
    }

    public static boolean isEnabled() {
        return getSharedPreferences().getBoolean(PREF_KEY_LOOP_ENABLED, false);
    }

    public static void setEnabled(boolean enabled) {
        getSharedPreferences().edit().putBoolean(PREF_KEY_LOOP_ENABLED, enabled).apply();
    }

    public static long getFeedItemId() {
        return getSharedPreferences().getLong(PREF_KEY_LOOP_FEED_ID, 0);
    }

    public static void setFeedItemId(long feedId) {
        getSharedPreferences().edit().putLong(PREF_KEY_LOOP_FEED_ID, feedId).apply();
    }

    public static int getStart() {
        return getSharedPreferences().getInt(PREF_KEY_LOOP_START, 0);
    }

    public static void setStart(int start) {
        getSharedPreferences().edit().putInt(PREF_KEY_LOOP_START, start).apply();
    }

    public static int getEnd() {
        return getSharedPreferences().getInt(PREF_KEY_LOOP_END, 0);
    }

    public static void setEnd(int end) {
        getSharedPreferences().edit().putInt(PREF_KEY_LOOP_END, end).apply();
    }

    public static SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        return sharedPreferences;
    }
}
