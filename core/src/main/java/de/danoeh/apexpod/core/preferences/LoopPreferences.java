package de.danoeh.apexpod.core.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class LoopPreferences {
    private static SharedPreferences sharedPreferences;
    public static final String PREF_KEY_LOOP_ENABLED = "loop_enabled";
    public static final String PREF_KEY_LOOP_FEED_ID = "loop_feed_id";
    public static final String PREF_KEY_LOOP_START = "loop_start";
    public static final String PREF_KEY_LOOP_END = "loop_end";

    public static void init(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isEnabled() {
        return sharedPreferences.getBoolean(PREF_KEY_LOOP_ENABLED, false);
    }

    public static void setEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(PREF_KEY_LOOP_ENABLED, enabled).apply();
    }

    public static long getFeedItemId() {
        return sharedPreferences.getLong(PREF_KEY_LOOP_FEED_ID, 0);
    }

    public static void setFeedItemId(long feedId) {
        sharedPreferences.edit().putLong(PREF_KEY_LOOP_FEED_ID, feedId).apply();
    }

    public static int getStart() {
        return sharedPreferences.getInt(PREF_KEY_LOOP_START, 0);
    }

    public static void setStart(int start) {
        sharedPreferences.edit().putInt(PREF_KEY_LOOP_START, start).apply();
    }

    public static int getEnd() {
        return sharedPreferences.getInt(PREF_KEY_LOOP_END, 0);
    }

    public static void setEnd(int end) {
        sharedPreferences.edit().putInt(PREF_KEY_LOOP_END, end).apply();
    }
}
