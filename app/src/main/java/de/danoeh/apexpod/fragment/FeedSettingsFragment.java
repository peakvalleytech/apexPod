package de.danoeh.apexpod.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.event.settings.SkipIntroEndingChangedEvent;
import de.danoeh.apexpod.core.event.settings.SpeedPresetChangedEvent;
import de.danoeh.apexpod.core.event.settings.VolumeAdaptionChangedEvent;
import de.danoeh.apexpod.databinding.PlaybackSpeedFeedSettingDialogBinding;
import de.danoeh.apexpod.model.feed.AutoDownload;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedFilter;
import de.danoeh.apexpod.model.feed.FeedPreferences;
import de.danoeh.apexpod.model.feed.VolumeAdaptionSetting;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.dialog.AuthenticationDialog;
import de.danoeh.apexpod.dialog.EpisodeFilterDialog;
import de.danoeh.apexpod.dialog.FeedPreferenceSkipDialog;
import de.danoeh.apexpod.dialog.TagSettingsDialog;
import io.reactivex.Maybe;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

public class FeedSettingsFragment extends Fragment {
    private static final String TAG = "FeedSettingsFragment";
    private static final String EXTRA_FEED_ID = "de.danoeh.apexpod.extra.feedId";

    private Disposable disposable;

    public static FeedSettingsFragment newInstance(Feed feed) {
        FeedSettingsFragment fragment = new FeedSettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(EXTRA_FEED_ID, feed.getId());
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.feedsettings, container, false);
        long feedId = getArguments().getLong(EXTRA_FEED_ID);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        getParentFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment_container,
                        FeedSettingsPreferenceFragment.newInstance(feedId), "settings_fragment")
                .commitAllowingStateLoss();

        disposable = Maybe.create((MaybeOnSubscribe<Feed>) emitter -> {
            Feed feed = DBReader.getFeed(feedId);
            if (feed != null) {
                emitter.onSuccess(feed);
            } else {
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> toolbar.setSubtitle(result.getTitle()),
                        error -> Log.d(TAG, Log.getStackTraceString(error)),
                        () -> { });


        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public static class FeedSettingsPreferenceFragment extends PreferenceFragmentCompat {
        private static final CharSequence PREF_SCREEN = "feedSettingsScreen";
        private static final CharSequence PREF_AUTHENTICATION = "authentication";
        private static final CharSequence PREF_AUTO_DELETE = "autoDelete";
        private static final CharSequence PREF_CATEGORY_AUTO_DOWNLOAD = "autoDownloadCategory";
        private static final String PREF_FEED_PLAYBACK_SPEED = "feedPlaybackSpeed";
        private static final String PREF_AUTO_SKIP = "feedAutoSkip";
        private static final String PREF_TAGS = "tags";
        private static final String PREF_AUTO_DOWNLOAD_CACHE = "autoDownloadCache";
        private static final String PREF_AUTO_DOWNLOAD_NEWEST_FIRST = "autoDownloadNewestFirst";
        private static final String PREF_AUTO_DOWNLOAD_INCLUDE_ALL = "autoDownloadIncludeAll";
        private static final CharSequence PREF_EPISODE_FILTER = "episodeFilter";

        private Feed feed;
        private Disposable disposable;
        private FeedPreferences feedPreferences;

        public static FeedSettingsPreferenceFragment newInstance(long feedId) {
            FeedSettingsPreferenceFragment fragment = new FeedSettingsPreferenceFragment();
            Bundle arguments = new Bundle();
            arguments.putLong(EXTRA_FEED_ID, feedId);
            fragment.setArguments(arguments);
            return fragment;
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
            final RecyclerView view = super.onCreateRecyclerView(inflater, parent, state);
            // To prevent transition animation because of summary update
            view.setItemAnimator(null);
            view.setLayoutAnimation(null);
            return view;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.feed_settings);
            // To prevent displaying partially loaded data
            findPreference(PREF_SCREEN).setVisible(false);

            long feedId = getArguments().getLong(EXTRA_FEED_ID);
            disposable = Maybe.create((MaybeOnSubscribe<Feed>) emitter -> {
                Feed feed = DBReader.getFeed(feedId);
                if (feed != null) {
                    emitter.onSuccess(feed);
                } else {
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        feed = result;
                        feedPreferences = feed.getPreferences();

                        setupAutoDownloadGlobalPreference();
                        setupAutoDownloadPreference();
                        setupKeepUpdatedPreference();
                        setupAutoDeletePreference();
                        setupVolumeReductionPreferences();
                        setupAuthentificationPreference();
                        setupEpisodeFilterPreference();
                        setupPlaybackSpeedPreference();
                        setupFeedAutoSkipPreference();
                        setupEpisodeNotificationPreference();
                        setupTags();

                        updateAutoDeleteSummary();
                        updateVolumeReductionValue();
                        updateAutoDownloadEnabled();

                        if (feed.isLocalFeed()) {
                            findPreference(PREF_AUTHENTICATION).setVisible(false);
                            findPreference(PREF_AUTO_DELETE).setVisible(false);
                            findPreference(PREF_CATEGORY_AUTO_DOWNLOAD).setVisible(false);
                        }

                        findPreference(PREF_SCREEN).setVisible(true);
                    }, error -> Log.d(TAG, Log.getStackTraceString(error)), () -> { });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (disposable != null) {
                disposable.dispose();
            }
        }

        private void setupFeedAutoSkipPreference() {
            findPreference(PREF_AUTO_SKIP).setOnPreferenceClickListener(preference -> {
                new FeedPreferenceSkipDialog(getContext(),
                        feedPreferences.getFeedSkipIntro(),
                        feedPreferences.getFeedSkipEnding()) {
                    @Override
                    protected void onConfirmed(int skipIntro, int skipEnding) {
                        feedPreferences.setFeedSkipIntro(skipIntro);
                        feedPreferences.setFeedSkipEnding(skipEnding);
                        DBWriter.setFeedPreferences(feedPreferences);
                        EventBus.getDefault().post(
                                new SkipIntroEndingChangedEvent(feedPreferences.getFeedSkipIntro(),
                                        feedPreferences.getFeedSkipEnding(),
                                        feed.getId()));
                    }
                }.show();
                return false;
            });
        }

        private void setupPlaybackSpeedPreference() {
            Preference feedPlaybackSpeedPreference = findPreference(PREF_FEED_PLAYBACK_SPEED);
            feedPlaybackSpeedPreference.setOnPreferenceClickListener(preference -> {
                PlaybackSpeedFeedSettingDialogBinding viewBinding =
                        PlaybackSpeedFeedSettingDialogBinding.inflate(getLayoutInflater());
                viewBinding.seekBar.setProgressChangedListener(speed ->
                        viewBinding.currentSpeedLabel.setText(String.format(Locale.getDefault(), "%.2fx", speed)));
                float speed = feedPreferences.getFeedPlaybackSpeed();
                viewBinding.useGlobalCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    viewBinding.seekBar.setEnabled(!isChecked);
                    viewBinding.seekBar.setAlpha(isChecked ? 0.4f : 1f);
                    viewBinding.currentSpeedLabel.setAlpha(isChecked ? 0.4f : 1f);
                });
                viewBinding.useGlobalCheckbox.setChecked(speed == FeedPreferences.SPEED_USE_GLOBAL);
                viewBinding.seekBar.updateSpeed(speed == FeedPreferences.SPEED_USE_GLOBAL ? 1 : speed);
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.playback_speed)
                        .setView(viewBinding.getRoot())
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            float newSpeed = viewBinding.useGlobalCheckbox.isChecked()
                                    ? FeedPreferences.SPEED_USE_GLOBAL : viewBinding.seekBar.getCurrentSpeed();
                            feedPreferences.setFeedPlaybackSpeed(newSpeed);
                            DBWriter.setFeedPreferences(feedPreferences);
                            EventBus.getDefault().post(
                                    new SpeedPresetChangedEvent(feedPreferences.getFeedPlaybackSpeed(), feed.getId()));
                        })
                        .setNegativeButton(R.string.cancel_label, null)
                        .show();
                return true;
            });
        }

        private void setupEpisodeFilterPreference() {
            findPreference(PREF_EPISODE_FILTER).setOnPreferenceClickListener(preference -> {
                new EpisodeFilterDialog(getContext(), feedPreferences.getFilter()) {
                    @Override
                    protected void onConfirmed(FeedFilter filter) {
                        feedPreferences.setFilter(filter);
                        DBWriter.setFeedPreferences(feedPreferences);
                    }
                }.show();
                return false;
            });
        }

        private void setupAuthentificationPreference() {
            findPreference(PREF_AUTHENTICATION).setOnPreferenceClickListener(preference -> {
                new AuthenticationDialog(getContext(),
                        R.string.authentication_label, true,
                        feedPreferences.getUsername(), feedPreferences.getPassword()) {
                    @Override
                    protected void onConfirmed(String username, String password) {
                        feedPreferences.setUsername(username);
                        feedPreferences.setPassword(password);
                        DBWriter.setFeedPreferences(feedPreferences);
                    }
                }.show();
                return false;
            });
        }

        private void setupAutoDeletePreference() {
            findPreference(PREF_AUTO_DELETE).setOnPreferenceChangeListener((preference, newValue) -> {
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
                DBWriter.setFeedPreferences(feedPreferences);
                updateAutoDeleteSummary();
                return false;
            });
        }

        private void updateAutoDeleteSummary() {
            ListPreference autoDeletePreference = findPreference(PREF_AUTO_DELETE);

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

        private void setupVolumeReductionPreferences() {
            ListPreference volumeReductionPreference = findPreference("volumeReduction");
            volumeReductionPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                switch ((String) newValue) {
                    case "off":
                        feedPreferences.setVolumeAdaptionSetting(VolumeAdaptionSetting.OFF);
                        break;
                    case "light":
                        feedPreferences.setVolumeAdaptionSetting(VolumeAdaptionSetting.LIGHT_REDUCTION);
                        break;
                    case "heavy":
                        feedPreferences.setVolumeAdaptionSetting(VolumeAdaptionSetting.HEAVY_REDUCTION);
                        break;
                }
                DBWriter.setFeedPreferences(feedPreferences);
                updateVolumeReductionValue();
                EventBus.getDefault().post(
                        new VolumeAdaptionChangedEvent(feedPreferences.getVolumeAdaptionSetting(), feed.getId()));
                return false;
            });
        }

        private void updateVolumeReductionValue() {
            ListPreference volumeReductionPreference = findPreference("volumeReduction");

            switch (feedPreferences.getVolumeAdaptionSetting()) {
                case OFF:
                    volumeReductionPreference.setValue("off");
                    break;
                case LIGHT_REDUCTION:
                    volumeReductionPreference.setValue("light");
                    break;
                case HEAVY_REDUCTION:
                    volumeReductionPreference.setValue("heavy");
                    break;
            }
        }

        private void setupKeepUpdatedPreference() {
            SwitchPreferenceCompat pref = findPreference("keepUpdated");

            pref.setChecked(feedPreferences.getKeepUpdated());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean checked = newValue == Boolean.TRUE;
                feedPreferences.setKeepUpdated(checked);
                DBWriter.setFeedPreferences(feedPreferences);
                pref.setChecked(checked);
                return false;
            });
        }
        private void setupAutoDownloadGlobalPreference() {
            if (!UserPreferences.isEnableAutodownload()) {
                SwitchPreferenceCompat autodl = findPreference("autoDownload");
                autodl.setChecked(false);
                autodl.setEnabled(false);
                autodl.setSummary(R.string.auto_download_disabled_globally);
                findPreference(PREF_EPISODE_FILTER).setEnabled(false);

            }
        }
        private void setupAutoDownloadPreference() {
            SwitchPreferenceCompat autoDownloadPrefView = findPreference("autoDownload");
            EditTextPreference autoDownloadCachePrefView = findPreference(PREF_AUTO_DOWNLOAD_CACHE);
            SwitchPreferenceCompat newestFirstPrefView = findPreference(PREF_AUTO_DOWNLOAD_NEWEST_FIRST);
            CheckBoxPreference includeAllPrefView = findPreference(PREF_AUTO_DOWNLOAD_INCLUDE_ALL);
            Preference episodeFilterPrefView = findPreference(PREF_EPISODE_FILTER);
            if (!UserPreferences.isEnableAutodownload()) {
                SwitchPreferenceCompat autodl = findPreference("autoDownload");
                boolean autoDownloadChecked = false;
                autodl.setChecked(autoDownloadChecked);
                autodl.setEnabled(autoDownloadChecked);
                autodl.setSummary(R.string.auto_download_disabled_globally);
                autoDownloadCachePrefView.setEnabled(autoDownloadChecked);
                newestFirstPrefView.setEnabled(autoDownloadChecked);
                includeAllPrefView.setEnabled(autoDownloadChecked);
                findPreference(PREF_EPISODE_FILTER).setEnabled(autoDownloadChecked);

            } else {
                if (feedPreferences == null)
                    return;

                boolean autoDownloadEnabled = feedPreferences.getAutoDownload();

                autoDownloadPrefView.setChecked(autoDownloadEnabled);
                autoDownloadPrefView.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean autoDownloadChecked = newValue == Boolean.TRUE;
                    feedPreferences.setAutoDownload(autoDownloadChecked);
                    DBWriter.setFeedPreferences(feedPreferences);
                    autoDownloadPrefView.setChecked(autoDownloadChecked);
                    autoDownloadCachePrefView.setEnabled(autoDownloadChecked);
                    newestFirstPrefView.setEnabled(autoDownloadChecked);
                    includeAllPrefView.setEnabled(autoDownloadChecked);
                    episodeFilterPrefView.setEnabled(autoDownloadChecked);
                    return false;
                });

                AutoDownload autoDownload = feedPreferences.getAutoDownloadPreferences();
                if (autoDownload == null) {
                    autoDownload = new AutoDownload(0, true, false);
                }
                autoDownloadCachePrefView.setEnabled(autoDownloadEnabled);
                AutoDownload finalAutoDownload = autoDownload;
                autoDownloadCachePrefView.setOnPreferenceChangeListener((preference, newValue) -> {
                    Integer cacheSize = 0;
                    String text = (String) newValue;
                    try {
                        cacheSize = Integer.valueOf(text);
                        if (cacheSize > feed.getItems().size()) {
                            cacheSize = 0;
                        }
                    } catch (NumberFormatException numberFormatException) {
                        Log.d(TAG, numberFormatException.getMessage() + " : setting cache size to 0");
                        cacheSize = 0;
                    }
                    finalAutoDownload.setCacheSize(cacheSize);
                    updateAutoDownload(finalAutoDownload);
                    autoDownloadCachePrefView.setSummary(getString(R.string.auto_download_cache_pref_summary) + cacheSize);
                    return true;
                });
                newestFirstPrefView.setEnabled(autoDownloadEnabled);
                newestFirstPrefView.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean newestFirst = newValue == Boolean.TRUE;
                    finalAutoDownload.setNewestFirst(newestFirst);
                    updateAutoDownload(finalAutoDownload);
                    return true;
                });
                includeAllPrefView.setEnabled(autoDownloadEnabled);
                includeAllPrefView.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean inncludeAll = newValue == Boolean.TRUE;
                    finalAutoDownload.setIncludeAll(inncludeAll);
                    updateAutoDownload(finalAutoDownload);
                    return true;
                });
                episodeFilterPrefView.setEnabled(autoDownloadEnabled);

                AutoDownload autoDownloadPrefs = feedPreferences.getAutoDownloadPreferences();
                if (autoDownloadPrefs != null) {
                    int autoDownloadCacheSize = autoDownloadPrefs.getCacheSize();
                    autoDownloadCachePrefView.setSummary(getString(R.string.auto_download_cache_pref_summary)
                            + autoDownloadCacheSize);
                    newestFirstPrefView.setChecked(autoDownloadPrefs.isNewestFirst());
                    includeAllPrefView.setChecked(autoDownloadPrefs.isIncludeAll());
                }
            }
        }

        private void updateAutoDownloadEnabled() {
            if (feed != null && feed.getPreferences() != null) {
                boolean enabled = feed.getPreferences().getAutoDownload() && UserPreferences.isEnableAutodownload();
                findPreference(PREF_EPISODE_FILTER).setEnabled(enabled);
            }
        }

        private void setupTags() {
            findPreference(PREF_TAGS).setOnPreferenceClickListener(preference -> {
                TagSettingsDialog.newInstance(feedPreferences).show(getChildFragmentManager(), TagSettingsDialog.TAG);
                return true;
            });
        }

        private void setupEpisodeNotificationPreference() {
            SwitchPreferenceCompat pref = findPreference("episodeNotification");

            pref.setChecked(feedPreferences.getShowEpisodeNotification());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean checked = newValue == Boolean.TRUE;
                feedPreferences.setShowEpisodeNotification(checked);
                DBWriter.setFeedPreferences(feedPreferences);
                pref.setChecked(checked);
                return false;
            });
        }

        private void updateAutoDownload(AutoDownload autoDownload) {
            feedPreferences.setAutoDownloadPreferences(autoDownload);
            DBWriter.setFeedPreferences(feedPreferences);

        }
    }
}
