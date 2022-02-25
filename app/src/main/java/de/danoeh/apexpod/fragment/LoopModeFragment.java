package de.danoeh.apexpod.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.preferences.LoopPreferences;
import de.danoeh.apexpod.core.preferences.PlaybackPreferences;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.util.Converter;
import de.danoeh.apexpod.core.util.playback.PlaybackController;
import de.danoeh.apexpod.core.util.playback.Timeline;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.model.playback.Playable;
import de.danoeh.apexpod.view.ShownotesWebView;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Displays the description of a Playable object in a Webview.
 */
public class LoopModeFragment extends Fragment {
    private static final String TAG = "LoopModeFragment";

    private static final String PREF = "ItemDescriptionFragmentPrefs";
    private static final String PREF_SCROLL_Y = "prefScrollY";
    private static final String PREF_PLAYABLE_ID = "prefPlayableId";

    private Disposable webViewLoader;
    private PlaybackController controller;

    private AppCompatCheckBox repeatEpisodeCheckbox;
    private AppCompatCheckBox repeatSectionCheckbox;
    private Button startButton;
    private Button endButton;
    private AppCompatEditText startField;
    private AppCompatEditText endField;

    int startPos = -1;
    int endPos = -1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Creating view");
        View root = inflater.inflate(R.layout.loop_mode_fragment, container, false);
        repeatEpisodeCheckbox = root.findViewById(R.id.checkbox_repeat_episode);
        repeatSectionCheckbox = root.findViewById(R.id.checkbox_repeat_section);
        startButton = root.findViewById(R.id.btnStart);
        endButton = root.findViewById(R.id.btnEnd);
        startField = root.findViewById(R.id.editTxtStart);
        endField = root.findViewById(R.id.editTxtEnd);
        startPos = 0;


        repeatEpisodeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
           updateMode(isChecked);
        });
        repeatSectionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateMode(!isChecked);
        });


        return root;
    }

    private void  updateMode(boolean repeatEpisode) {
        if (repeatEpisode) {
            repeatEpisodeCheckbox.setChecked(true);
            repeatSectionCheckbox.setChecked(false);
            startButton.setEnabled(false);
            endButton.setEnabled(false);
            startField.setEnabled(false);
            endField.setEnabled(false);
        } else {
            repeatSectionCheckbox.setChecked(true);
            repeatEpisodeCheckbox.setChecked(false);
            startButton.setEnabled(true);
            endButton.setEnabled(true);
            startField.setEnabled(true);
            endField.setEnabled(true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (controller != null) {
            endPos = controller.getDuration();
        }
        startButton.setOnClickListener(v -> {
            int tmpStartPos = controller.getPosition();
            if (tmpStartPos >= endPos) {
                Log.d(TAG, "Error: start position must be less than end position");
            } else {
                Log.d(TAG, "Setting loop start at position " + Converter.getDurationStringLong(tmpStartPos));
                startPos = tmpStartPos;
            }
        });

        endButton.setOnClickListener(v -> {
            int tmpEndPos = controller.getPosition();
            if (startPos >= tmpEndPos) {
                Log.d(TAG, "Error: start position must be less than end position");
            } else {
                Log.d(TAG, "Setting loop end at position " + Converter.getDurationStringLong(tmpEndPos));
                endPos = tmpEndPos;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment destroyed");
    }


    private void load() {
        Log.d(TAG, "load()");
        if (webViewLoader != null) {
            webViewLoader.dispose();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreference();
    }

    private void savePreference() {
        Log.d(TAG, "Saving preferences");
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
//        if (controller != null && controller.getMedia() != null && webvDescription != null) {
//            Log.d(TAG, "Saving scroll position: " + webvDescription.getScrollY());
//            editor.putInt(PREF_SCROLL_Y, webvDescription.getScrollY());
//            editor.putString(PREF_PLAYABLE_ID, controller.getMedia().getIdentifier()
//                    .toString());
//        } else {
//            Log.d(TAG, "savePreferences was called while media or webview was null");
//            editor.putInt(PREF_SCROLL_Y, -1);
//            editor.putString(PREF_PLAYABLE_ID, "");
//        }
        editor.apply();
    }

    private boolean restoreFromPreference() {
        Log.d(TAG, "Restoring from preferences");
        Activity activity = getActivity();
        if (activity != null) {
            SharedPreferences prefs = activity.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
            String id = prefs.getString(PREF_PLAYABLE_ID, "");
            int scrollY = prefs.getInt(PREF_SCROLL_Y, -1);
//            if (controller != null && scrollY != -1 && controller.getMedia() != null
//                    && id.equals(controller.getMedia().getIdentifier().toString())
//                    && webvDescription != null) {
//                Log.d(TAG, "Restored scroll Position: " + scrollY);
//                webvDescription.scrollTo(webvDescription.getScrollX(), scrollY);
//                return true;
//            }
        }
        return false;
    }

    public void scrollToTop() {
        savePreference();
    }

    @Override
    public void onStart() {
        super.onStart();
        controller = new PlaybackController(getActivity()) {
            @Override
            public void loadMediaInfo() {
                load();
            }
        };
        controller.init();
        load();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (webViewLoader != null) {
            webViewLoader.dispose();
        }
        controller.release();
        controller = null;
    }
}
