package de.danoeh.apexpod.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.preferences.LoopPreferences;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.util.Converter;
import de.danoeh.apexpod.core.util.playback.PlaybackController;
import io.reactivex.disposables.Disposable;

/**
 * Displays the description of a Playable object in a Webview.
 */
public class LoopModeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "LoopModeFragment";

    private static final String PREF = "ItemDescriptionFragmentPrefs";
    private static final String PREF_SCROLL_Y = "prefScrollY";
    private static final String PREF_PLAYABLE_ID = "prefPlayableId";

    private Disposable webViewLoader;
    private PlaybackController controller;

    private SwitchCompat repeatModeSwitch;
    private AppCompatCheckBox repeatEpisodeCheckbox;
    private AppCompatCheckBox repeatSectionCheckbox;
    private Button startButton;
    private Button endButton;
    private AppCompatEditText startField;
    private AppCompatEditText endField;
    boolean repeatEnabled;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Creating view");
        View root = inflater.inflate(R.layout.loop_mode_fragment, container, false);
        repeatModeSwitch = root.findViewById((R.id.switch_enable_repeat));
        repeatEpisodeCheckbox = root.findViewById(R.id.checkbox_repeat_episode);
        repeatSectionCheckbox = root.findViewById(R.id.checkbox_repeat_section);
        startButton = root.findViewById(R.id.btnStart);
        endButton = root.findViewById(R.id.btnEnd);
        startField = root.findViewById(R.id.editTxtStart);
        endField = root.findViewById(R.id.editTxtEnd);

        // Repeat episode preference allows either to repeat the episode
        // or optionally to repeat a section (loop) which is set using
        // LoopPreferences.setEnabled()
        repeatEnabled= UserPreferences.getShouldRepeatEpisode();
        setLoopModeView(repeatEnabled, LoopPreferences.isEnabled());

        repeatModeSwitch.setOnClickListener(v -> {
            boolean isChecked = repeatModeSwitch.isChecked();
            UserPreferences.setShouldRepeatEpisode(isChecked);
            setLoopModeView(isChecked, false);
        });


        repeatEpisodeCheckbox.setOnClickListener(v -> {
           setLoopModeView(repeatEnabled, !repeatEpisodeCheckbox.isChecked());
        });

        repeatSectionCheckbox.setOnClickListener(v -> {
            setLoopModeView(repeatEnabled, repeatSectionCheckbox.isChecked());
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return root;
    }

    private void setLoopModeView(boolean repeat, boolean loop) {
        if (!repeat) {
            disabledRepeatSwitch(repeat);
        } else {
            repeatModeSwitch.setChecked(repeat);
            if (!loop) {
                repeatEpisodeCheckbox.setEnabled(true);
                repeatEpisodeCheckbox.setChecked(true);
                repeatSectionCheckbox.setEnabled(true);
                repeatSectionCheckbox.setChecked(false);
                startButton.setEnabled(false);
                endButton.setEnabled(false);
                startField.setEnabled(false);
                endField.setEnabled(false);
                setTextFields(0, 0);
            } else {
                repeatSectionCheckbox.setEnabled(true);
                repeatSectionCheckbox.setChecked(true);
                repeatEpisodeCheckbox.setChecked(false);
                startButton.setEnabled(true);
                endButton.setEnabled(true);
                startField.setEnabled(true);
                endField.setEnabled(true);
                setTextFields(0, controller.getDuration());
            }
        }
    }

    private void disabledRepeatSwitch(boolean enabled) {
        repeatModeSwitch.setChecked(false);
        repeatSectionCheckbox.setChecked(enabled);
        repeatEpisodeCheckbox.setChecked(enabled);
        repeatSectionCheckbox.setEnabled(enabled);
        repeatEpisodeCheckbox.setEnabled(enabled);
        startButton.setEnabled(enabled);
        endButton.setEnabled(enabled);
        startField.setEnabled(enabled);
        endField.setEnabled(enabled);
    }

    private void setTextFields(int startTime, int endTime) {
        String startTimeText = Converter.getDurationStringLong(startTime);
        String endTimeText = Converter.getDurationStringLong(endTime);
        startField.setText(startTimeText);
        endField.setText(endTimeText);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startButton.setOnClickListener(v -> {
            int tmpStartPos = controller.getPosition();
            int endPos = LoopPreferences.getEnd();
            if (tmpStartPos >= endPos) {
                Log.d(TAG, "Error: start position must be less than end position");
            } else {
                String startText = Converter.getDurationStringLong(tmpStartPos);
                Log.d(TAG, "Setting loop start at position " + startText);
                startField.setText(startText);
                LoopPreferences.setStart(tmpStartPos);
            }
        });

        endButton.setOnClickListener(v -> {
            int tmpEndPos = controller.getPosition();
            int startPos = LoopPreferences.getStart();
            if (startPos >= tmpEndPos) {
                Log.d(TAG, "Error: start position must be less than end position");
            } else {
                String endText = Converter.getDurationStringLong(tmpEndPos);
                Log.d(TAG, "Setting loop end at position " + endText);
                endField.setText(endText);
                LoopPreferences.setEnd(tmpEndPos);
            }
        });
    }

    public LoopModeFragment(PlaybackController controller) {
        super();
        this.controller = controller;
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

        controller.release();
        controller = null;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(UserPreferences.PREF_REPEAT_EPISODE)) {
            repeatEnabled = UserPreferences.getShouldRepeatEpisode();
            setLoopModeView(repeatEnabled, LoopPreferences.isEnabled());
        }
    }
}
