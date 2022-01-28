package de.danoeh.apexpod.fragment.preferences;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.PreferenceActivity;
import de.danoeh.apexpod.core.preferences.UserPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DownloadPreferencesFragment extends PreferenceFragmentCompat {
    private static final String TAG = "DwnldPrefFragment";

    private CheckBoxPreference[] selectedNetworks;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_download);

        setupAutoDownloadScreen();
        buildAutodownloadSelectedNetworksPreference();
        setSelectedNetworksEnabled(UserPreferences.isEnableAutodownloadWifiFilter());
        findPreference(UserPreferences.PREF_PARALLEL_DOWNLOADS)
                .setOnPreferenceChangeListener(
                        (preference, o) -> {
                            if (o instanceof Integer) {
                                setParallelDownloadsText((Integer) o);
                            }
                            return true;
                        }
                );
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.pref_download_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAutodownloadItemVisibility(UserPreferences.isEnableAutodownload());
        setParallelDownloadsText(UserPreferences.getParallelDownloads());
    }

    private void setupAutoDownloadScreen() {
        Preference autoDownloadPref = findPreference(UserPreferences.PREF_ENABLE_AUTODL);
        autoDownloadPref.setDefaultValue(UserPreferences.isEnableAutodownload());
        autoDownloadPref.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    if (newValue instanceof Boolean) {
                        checkAutodownloadItemVisibility((Boolean) newValue);
                    }
                    return true;
                });
        if (Build.VERSION.SDK_INT >= 29) {
            findPreference(UserPreferences.PREF_ENABLE_AUTODL_WIFI_FILTER).setVisible(false);
        }
        findPreference(UserPreferences.PREF_ENABLE_AUTODL_WIFI_FILTER)
                .setOnPreferenceChangeListener(
                        (preference, newValue) -> {
                            if (newValue instanceof Boolean) {
                                setSelectedNetworksEnabled((Boolean) newValue);
                                return true;
                            } else {
                                return false;
                            }
                        }
                );
    }

    private void checkAutodownloadItemVisibility(boolean autoDownload) {
//        findPreference(UserPreferences.PREF_EPISODE_CACHE_SIZE).setEnabled(autoDownload);
        findPreference(UserPreferences.PREF_ENABLE_AUTODL_ON_BATTERY).setEnabled(autoDownload);
        findPreference(UserPreferences.PREF_ENABLE_AUTODL_WIFI_FILTER).setEnabled(autoDownload);
        setSelectedNetworksEnabled(autoDownload && UserPreferences.isEnableAutodownloadWifiFilter());
    }

    private static String blankIfNull(String val) {
        return val == null ? "" : val;
    }

    @SuppressLint("MissingPermission") // getConfiguredNetworks needs location permission starting with API 29
    private void buildAutodownloadSelectedNetworksPreference() {
        if (Build.VERSION.SDK_INT >= 29) {
            return;
        }

        final Activity activity = getActivity();

        if (selectedNetworks != null) {
            clearAutodownloadSelectedNetworsPreference();
        }
        // get configured networks
        WifiManager wifiservice = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> networks = wifiservice.getConfiguredNetworks();

        if (networks == null) {
            Log.e(TAG, "Couldn't get list of configure Wi-Fi networks");
            return;
        }
        Collections.sort(networks, (x, y) ->
                blankIfNull(x.SSID).compareToIgnoreCase(blankIfNull(y.SSID)));
        selectedNetworks = new CheckBoxPreference[networks.size()];
        List<String> prefValues = Arrays.asList(UserPreferences
                .getAutodownloadSelectedNetworks());
        PreferenceScreen prefScreen = getPreferenceScreen();
        Preference.OnPreferenceClickListener clickListener = preference -> {
            if (preference instanceof CheckBoxPreference) {
                String key = preference.getKey();
                List<String> prefValuesList = new ArrayList<>(
                        Arrays.asList(UserPreferences
                                .getAutodownloadSelectedNetworks())
                );
                boolean newValue = ((CheckBoxPreference) preference)
                        .isChecked();
                Log.d(TAG, "Selected network " + key + ". New state: " + newValue);

                int index = prefValuesList.indexOf(key);
                if (index >= 0 && !newValue) {
                    // remove network
                    prefValuesList.remove(index);
                } else if (index < 0 && newValue) {
                    prefValuesList.add(key);
                }

                UserPreferences.setAutodownloadSelectedNetworks(prefValuesList.toArray(new String[0]));
                return true;
            } else {
                return false;
            }
        };
        // create preference for each known network. attach listener and set
        // value
        for (int i = 0; i < networks.size(); i++) {
            WifiConfiguration config = networks.get(i);

            CheckBoxPreference pref = new CheckBoxPreference(activity);
            String key = Integer.toString(config.networkId);
            pref.setTitle(config.SSID);
            pref.setKey(key);
            pref.setOnPreferenceClickListener(clickListener);
            pref.setPersistent(false);
            pref.setChecked(prefValues.contains(key));
            selectedNetworks[i] = pref;
            prefScreen.addPreference(pref);
        }
    }

    private void clearAutodownloadSelectedNetworsPreference() {
        if (selectedNetworks != null) {
            PreferenceScreen prefScreen = getPreferenceScreen();

            for (CheckBoxPreference network : selectedNetworks) {
                if (network != null) {
                    prefScreen.removePreference(network);
                }
            }
        }
    }

    private void setParallelDownloadsText(int downloads) {
        final Resources res = getActivity().getResources();
        String s = res.getString(R.string.parallel_downloads, downloads);
        findPreference(UserPreferences.PREF_PARALLEL_DOWNLOADS).setSummary(s);
    }

    private void setSelectedNetworksEnabled(boolean b) {
        if (selectedNetworks != null) {
            for (Preference p : selectedNetworks) {
                p.setEnabled(b);
            }
        }
    }

}
