package de.danoeh.apexpod.fragment.preferences;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.PreferenceActivity;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.dialog.ChooseDataFolderDialog;

import java.io.File;

public class StoragePreferencesFragment extends PreferenceFragmentCompat {
    private static final String TAG = "StoragePrefFragment";
    private static final String PREF_CHOOSE_DATA_DIR = "prefChooseDataDir";
    private static final String PREF_IMPORT_EXPORT = "prefImportExport";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_storage);
        setupStorageScreen();
        buildEpisodeCleanupPreference();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.storage_pref);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDataFolderText();
    }

    private void setupStorageScreen() {
        findPreference(PREF_CHOOSE_DATA_DIR).setOnPreferenceClickListener(
                preference -> {
                    ChooseDataFolderDialog.showDialog(getContext(), path -> {
                        UserPreferences.setDataFolder(path);
                        setDataFolderText();
                    });
                    return true;
                }
        );

        findPreference(UserPreferences.PREF_AUTO_DELETE).setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    if (newValue instanceof Boolean) {
//                        checkAutodownloadItemVisibility((Boolean) newValue);
                    }
                    return true;
                });
        findPreference(PREF_IMPORT_EXPORT).setOnPreferenceClickListener(
                preference -> {
                    ((PreferenceActivity) getActivity()).openScreen(R.xml.preferences_import_export);
                    return true;
                }
        );
    }

    private void setDataFolderText() {
        File f = UserPreferences.getDataFolder(null);
        if (f != null) {
            findPreference(PREF_CHOOSE_DATA_DIR).setSummary(f.getAbsolutePath());
        }
    }

    private void buildEpisodeCleanupPreference() {
        final Resources res = getActivity().getResources();

        ListPreference pref = findPreference(UserPreferences.PREF_EPISODE_CLEANUP);
        String[] values = res.getStringArray(
                R.array.episode_cleanup_values);
        String[] entries = new String[values.length];
        for (int x = 0; x < values.length; x++) {
            int v = Integer.parseInt(values[x]);
            if (v == UserPreferences.EPISODE_CLEANUP_EXCEPT_FAVORITE) {
                entries[x] =  res.getString(R.string.episode_cleanup_except_favorite_removal);
            } else if (v == UserPreferences.EPISODE_CLEANUP_QUEUE) {
                entries[x] = res.getString(R.string.episode_cleanup_queue_removal);
            } else if (v == UserPreferences.EPISODE_CLEANUP_NULL){
                entries[x] = res.getString(R.string.episode_cleanup_never);
            } else if (v == 0) {
                entries[x] = res.getString(R.string.episode_cleanup_after_listening);
            } else if (v > 0 && v < 24) {
                entries[x] = res.getQuantityString(R.plurals.episode_cleanup_hours_after_listening, v, v);
            } else {
                int numDays = v / 24; // assume underlying value will be NOT fraction of days, e.g., 36 (hours)
                entries[x] = res.getQuantityString(R.plurals.episode_cleanup_days_after_listening, numDays, numDays);
            }
        }
        pref.setEntries(entries);
    }
}
