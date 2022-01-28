package de.danoeh.apexpod.core.preferences;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class QueuePreferences {

    private static String TAG = "UserPreferences";

    private static Context context;
    private static SharedPreferences prefs;

    private static PREF_
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


}
