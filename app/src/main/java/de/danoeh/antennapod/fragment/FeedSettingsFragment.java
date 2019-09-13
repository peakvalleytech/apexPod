package de.danoeh.antennapod.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.dialog.ConfirmationDialog;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.feed.FeedFilter;
import de.danoeh.antennapod.core.feed.FeedPreferences;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.dialog.AuthenticationDialog;
import de.danoeh.antennapod.dialog.EpisodeFilterDialog;
import de.danoeh.antennapod.viewmodel.FeedSettingsViewModel;

import static de.danoeh.antennapod.activity.FeedSettingsActivity.EXTRA_FEED_ID;
import static de.danoeh.antennapod.core.feed.FeedPreferences.SPEED_USE_GLOBAL;

public class FeedSettingsFragment extends PreferenceFragmentCompat {
    private static final CharSequence PREF_EPISODE_FILTER = "episodeFilter";
    private static final String PREF_FEED_PLAYBACK_SPEED = "feedPlaybackSpeed";
    private Feed feed;
    private FeedPreferences feedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.feed_settings);

        long feedId = getArguments().getLong(EXTRA_FEED_ID);
        ViewModelProviders.of(getActivity()).get(FeedSettingsViewModel.class).getFeed(feedId)
                .subscribe(result -> {
                    feed = result;
                    feedPreferences = feed.getPreferences();

                    setupAutoDownloadPreference();
                    setupKeepUpdatedPreference();
                    setupAutoDeletePreference();
                    setupAuthentificationPreference();
                    setupEpisodeFilterPreference();
                    setupPlaybackSpeedPreference();

                    updateAutoDeleteSummary();
                    updateAutoDownloadEnabled();
                    updatePlaybackSpeedPreference();
                }).dispose();
    }

    private void setupPlaybackSpeedPreference() {
        ListPreference feedPlaybackSpeedPreference = (ListPreference) findPreference(PREF_FEED_PLAYBACK_SPEED);

        String[] speeds = UserPreferences.getPlaybackSpeedArray();

        String[] values = new String[speeds.length + 1];
        values[0] = Float.toString(SPEED_USE_GLOBAL);

        String[] entries = new String[speeds.length + 1];
        entries[0] = getString(R.string.feed_auto_download_global);

        System.arraycopy(speeds, 0, values, 1, speeds.length);
        System.arraycopy(speeds, 0, entries, 1, speeds.length);

        feedPlaybackSpeedPreference.setEntryValues(values);
        feedPlaybackSpeedPreference.setEntries(entries);

        feedPlaybackSpeedPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            feedPreferences.setFeedPlaybackSpeed(Float.parseFloat((String) newValue));
            feed.savePreferences();
            updatePlaybackSpeedPreference();
            return false;
        });
    }

    private void setupEpisodeFilterPreference() {
        findPreference(PREF_EPISODE_FILTER).setOnPreferenceClickListener(preference -> {
            new EpisodeFilterDialog(getContext(), feedPreferences.getFilter()) {
                @Override
                protected void onConfirmed(FeedFilter filter) {
                    feedPreferences.setFilter(filter);
                    feed.savePreferences();
                }
            }.show();
            return false;
        });
    }

    private void setupAuthentificationPreference() {
        findPreference("authentication").setOnPreferenceClickListener(preference -> {
            new AuthenticationDialog(getContext(),
                    R.string.authentication_label, true, false,
                    feedPreferences.getUsername(), feedPreferences.getPassword()) {
                @Override
                protected void onConfirmed(String username, String password, boolean saveUsernamePassword) {
                    feedPreferences.setUsername(username);
                    feedPreferences.setPassword(password);
                    feed.savePreferences();
                }
            }.show();
            return false;
        });
    }

    private void setupAutoDeletePreference() {
        ListPreference autoDeletePreference = (ListPreference) findPreference("autoDelete");
        autoDeletePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            switch ((String) newValue) {
                case "global":
                    feedPreferences.setAutoDeleteAction(FeedPreferences.AutoDeleteAction.GLOBAL);
                    break;
                case "always":
                    feedPreferences.setAutoDeleteAction(FeedPreferences.AutoDeleteAction.YES);
                    break;
                case "never":
                    feedPreferences.setAutoDeleteAction(FeedPreferences.AutoDeleteAction.NO);
                    break;
            }
            feed.savePreferences();
            updateAutoDeleteSummary();
            return false;
        });
    }

    private void updatePlaybackSpeedPreference() {
        ListPreference feedPlaybackSpeedPreference = (ListPreference) findPreference(PREF_FEED_PLAYBACK_SPEED);

        float speedValue = feedPreferences.getFeedPlaybackSpeed();
        feedPlaybackSpeedPreference.setValue(Float.toString(speedValue));
    }

    private void updateAutoDeleteSummary() {
        ListPreference autoDeletePreference = (ListPreference) findPreference("autoDelete");

        switch (feedPreferences.getAutoDeleteAction()) {
            case GLOBAL:
                autoDeletePreference.setSummary(R.string.feed_auto_download_global);
                autoDeletePreference.setValue("global");
                break;
            case YES:
                autoDeletePreference.setSummary(R.string.feed_auto_download_always);
                autoDeletePreference.setValue("always");
                break;
            case NO:
                autoDeletePreference.setSummary(R.string.feed_auto_download_never);
                autoDeletePreference.setValue("never");
                break;
        }
    }

    private void setupKeepUpdatedPreference() {
        SwitchPreference pref = (SwitchPreference) findPreference("keepUpdated");

        pref.setChecked(feedPreferences.getKeepUpdated());
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean checked = newValue == Boolean.TRUE;
            feedPreferences.setKeepUpdated(checked);
            feed.savePreferences();
            pref.setChecked(checked);
            return false;
        });
    }

    private void setupAutoDownloadPreference() {
        SwitchPreference pref = (SwitchPreference) findPreference("autoDownload");

        pref.setEnabled(UserPreferences.isEnableAutodownload());
        if (UserPreferences.isEnableAutodownload()) {
            pref.setChecked(feedPreferences.getAutoDownload());
        } else {
            pref.setChecked(false);
            pref.setSummary(R.string.auto_download_disabled_globally);
        }

        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean checked = newValue == Boolean.TRUE;

            feedPreferences.setAutoDownload(checked);
            feed.savePreferences();
            updateAutoDownloadEnabled();
            ApplyToEpisodesDialog dialog = new ApplyToEpisodesDialog(getActivity(), checked);
            dialog.createNewDialog().show();
            pref.setChecked(checked);
            return false;
        });
    }

    private void updateAutoDownloadEnabled() {
        if (feed != null && feed.getPreferences() != null) {
            boolean enabled = feed.getPreferences().getAutoDownload() && UserPreferences.isEnableAutodownload();
            findPreference(PREF_EPISODE_FILTER).setEnabled(enabled);
        }
    }

    private class ApplyToEpisodesDialog extends ConfirmationDialog {
        private final boolean autoDownload;

        ApplyToEpisodesDialog(Context context, boolean autoDownload) {
            super(context, R.string.auto_download_apply_to_items_title,
                    R.string.auto_download_apply_to_items_message);
            this.autoDownload = autoDownload;
            setPositiveText(R.string.yes);
            setNegativeText(R.string.no);
        }

        @Override
        public  void onConfirmButtonPressed(DialogInterface dialog) {
            DBWriter.setFeedsItemsAutoDownload(feed, autoDownload);
        }
    }
}
