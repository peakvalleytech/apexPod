package de.danoeh.apexpod.preferences;

import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.fragment.preferences.PlaybackPreferencesFragment;

/**
 * Implements functions from PreferenceController that are flavor dependent.
 */
public class PreferenceControllerFlavorHelper {

    public static void setupFlavoredUI(PlaybackPreferencesFragment ui) {
        ui.findPreference(UserPreferences.PREF_CAST_ENABLED).setEnabled(false);
    }
}
