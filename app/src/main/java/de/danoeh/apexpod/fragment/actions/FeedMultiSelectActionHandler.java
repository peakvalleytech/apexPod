package de.danoeh.apexpod.fragment.actions;

import android.util.Log;

import androidx.annotation.PluralsRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.NavDrawerData;
import de.danoeh.apexpod.databinding.PlaybackSpeedFeedSettingDialogBinding;
import de.danoeh.apexpod.dialog.RemoveFeedDialog;
import de.danoeh.apexpod.dialog.preferences.PreferenceListDialog;
import de.danoeh.apexpod.fragment.preferences.dialog.PreferenceSwitchDialog;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedPreferences;
import de.danoeh.apexpod.dialog.preferences.PreferenceAutoCompleteTextDialog;

public class FeedMultiSelectActionHandler {
    private static final String TAG = "FeedSelectHandler";
    private final MainActivity activity;
    private final List<Feed> selectedItems;

    public FeedMultiSelectActionHandler(MainActivity activity, List<Feed> selectedItems) {
        this.activity = activity;
        this.selectedItems = selectedItems;
    }

    public void handleAction(int id) {
        if (id == R.id.remove_item) {
            RemoveFeedDialog.show(activity, selectedItems, null);
        } else if (id == R.id.keep_updated) {
            keepUpdatedPrefHandler();
        } else if (id == R.id.autodownload) {
            autoDownloadPrefHandler();
        } else if (id == R.id.playback_speed) {
            playbackSpeedPrefHandler();
        } else if (id == R.id.add_tag) {
            addTagPrefHandler();
        } else if (id == R.id.remove_tag) {
            removeTagPrefHandler();
        } else {
            Log.e(TAG, "Unrecognized speed dial action item. Do nothing. id=" + id);
        }
    }

    private void addTagPrefHandler() {
        DialogFragment TagDialog = new PreferenceAutoCompleteTextDialog("Add tag",
                this::loadAutoCompleteTags, tag -> {
            saveFeedPreferences(feedPreferences -> {
              feedPreferences.addTag(tag);
            });
        });
        TagDialog.show(activity.getSupportFragmentManager(), null);
    }

    private void removeTagPrefHandler() {
        DialogFragment TagDialog = new PreferenceAutoCompleteTextDialog("Remove tag",
                this::loadAutoCompleteTags, tag -> {
            saveFeedPreferences(feedPreferences -> {
                feedPreferences.removeTag(tag);
            });
        });
        TagDialog.show(activity.getSupportFragmentManager(), null);
    }

    private void autoDownloadPrefHandler() {
        PreferenceSwitchDialog preferenceSwitchDialog = new PreferenceSwitchDialog(activity,
                activity.getString(R.string.auto_download_settings_label),
                activity.getString(R.string.auto_download_label));
        preferenceSwitchDialog.setOnPreferenceChangedListener(new PreferenceSwitchDialog.OnPreferenceChangedListener() {
            @Override
            public void preferenceChanged(boolean enabled) {
                saveFeedPreferences(feedPreferences -> feedPreferences.setAutoDownload(enabled));
            }
        });
        preferenceSwitchDialog.openDialog();
    }

    private static final DecimalFormat SPEED_FORMAT =
            new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private void playbackSpeedPrefHandler() {
        PlaybackSpeedFeedSettingDialogBinding viewBinding =
                PlaybackSpeedFeedSettingDialogBinding.inflate(activity.getLayoutInflater());
        viewBinding.seekBar.setProgressChangedListener(speed ->
                viewBinding.currentSpeedLabel.setText(String.format(Locale.getDefault(), "%.2fx", speed)));
        viewBinding.useGlobalCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewBinding.seekBar.setEnabled(!isChecked);
            viewBinding.seekBar.setAlpha(isChecked ? 0.4f : 1f);
            viewBinding.currentSpeedLabel.setAlpha(isChecked ? 0.4f : 1f);
        });
        viewBinding.seekBar.updateSpeed(1.0f);
        new AlertDialog.Builder(activity)
                .setTitle(R.string.playback_speed)
                .setView(viewBinding.getRoot())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    float newSpeed = viewBinding.useGlobalCheckbox.isChecked()
                            ? FeedPreferences.SPEED_USE_GLOBAL : viewBinding.seekBar.getCurrentSpeed();
                    saveFeedPreferences(feedPreferences -> feedPreferences.setFeedPlaybackSpeed(newSpeed));
                })
                .setNegativeButton(R.string.cancel_label, null)
                .show();
    }

    private void keepUpdatedPrefHandler() {
        PreferenceSwitchDialog preferenceSwitchDialog = new PreferenceSwitchDialog(activity,
                activity.getString(R.string.kept_updated),
                activity.getString(R.string.keep_updated_summary));
        preferenceSwitchDialog.setOnPreferenceChangedListener(keepUpdated -> {
            saveFeedPreferences(feedPreferences -> {
                feedPreferences.setKeepUpdated(keepUpdated);
            });
        });
        preferenceSwitchDialog.openDialog();
    }

    private List<String> loadAutoCompleteTags() {
        NavDrawerData data = DBReader.getNavDrawerData();
        List<NavDrawerData.DrawerItem> items = data.items;
        List<String> folders = new ArrayList<String>();
        for (NavDrawerData.DrawerItem item : items) {
            if (item.type == NavDrawerData.DrawerItem.Type.TAG) {
                NavDrawerData.TagDrawerItem tagItem = (NavDrawerData.TagDrawerItem) item;
                if (!tagItem.name.equals(FeedPreferences.TAG_ROOT))
                    folders.add(item.getTitle());
            }
        }
        return folders;
    }

    private void showMessage(@PluralsRes int msgId, int numItems) {
        activity.showSnackbarAbovePlayer(activity.getResources()
                .getQuantityString(msgId, numItems, numItems), Snackbar.LENGTH_LONG);
    }

    private void saveFeedPreferences(Consumer<FeedPreferences> preferencesConsumer) {
        for (Feed feed : selectedItems) {
            preferencesConsumer.accept(feed.getPreferences());
            DBWriter.setFeedPreferences(feed.getPreferences());
        }
        showMessage(R.plurals.updated_feeds_batch_label, selectedItems.size());
    }
}
