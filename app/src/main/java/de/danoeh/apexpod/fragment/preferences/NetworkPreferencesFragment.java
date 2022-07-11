package de.danoeh.apexpod.fragment.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.DateFormat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.PreferenceActivity;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.dialog.FeedRefreshIntervalDialog;
import de.danoeh.apexpod.dialog.ProxyDialog;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


public class NetworkPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String PREF_PROXY = "prefProxy";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_network);
        setupNetworkScreen();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.network_pref);
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpdateIntervalText();
    }

    private void setupNetworkScreen() {
        findPreference(UserPreferences.PREF_UPDATE_INTERVAL)
                .setOnPreferenceClickListener(preference -> {
                    new FeedRefreshIntervalDialog(getContext()).show();
                    return true;
                });

        // validate and set correct value: number of downloads between 1 and 50 (inclusive)
        findPreference(PREF_PROXY).setOnPreferenceClickListener(preference -> {
            ProxyDialog dialog = new ProxyDialog(getActivity());
            dialog.show();
            return true;
        });
    }

    /**
     *  Used to init and handle changes to view
      */
    private void setUpdateIntervalText() {
        Context context = getActivity().getApplicationContext();
        String val;
        long interval = UserPreferences.getUpdateInterval();
        if (interval > 0) {
            int hours = (int) TimeUnit.MILLISECONDS.toHours(interval);
            val = context.getResources().getQuantityString(
                    R.plurals.feed_refresh_every_x_hours, hours, hours);
        } else {
            int[] timeOfDay = UserPreferences.getUpdateTimeOfDay();
            if (timeOfDay.length == 2) {
                Calendar cal = new GregorianCalendar();
                cal.set(Calendar.HOUR_OF_DAY, timeOfDay[0]);
                cal.set(Calendar.MINUTE, timeOfDay[1]);
                String timeOfDayStr = DateFormat.getTimeFormat(context).format(cal.getTime());
                val = String.format(context.getString(R.string.feed_refresh_interval_at),
                        timeOfDayStr);
            } else {
                val = context.getString(R.string.never);
            }
        }
        String summary = context.getString(R.string.feed_refresh_sum) + "\n"
                + String.format(context.getString(R.string.pref_current_value), val);
        findPreference(UserPreferences.PREF_UPDATE_INTERVAL).setSummary(summary);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (UserPreferences.PREF_UPDATE_INTERVAL.equals(key)) {
            setUpdateIntervalText();
        }
    }
}


