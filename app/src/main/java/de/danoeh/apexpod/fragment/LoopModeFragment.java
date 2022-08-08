package de.danoeh.apexpod.fragment;

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
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.model.playback.Playable;
import io.reactivex.disposables.Disposable;

/**
 * Displays the description of a Playable object in a Webview.
 */
public class LoopModeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "LoopModeFragment";
    private PlaybackController controller;
    private SwitchCompat repeatModeSwitch;
    private AppCompatCheckBox repeatEpisodeCheckbox;
    private AppCompatCheckBox repeatSectionCheckbox;
    private Button startButton;
    private Button endButton;
    private AppCompatEditText startField;
    private AppCompatEditText endField;
    private Button resetButton;
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
        resetButton = root.findViewById(R.id.resetButton);




        return root;
    }



    private void setLoopModeView(boolean repeat, boolean loop) {
        if (!repeat) {
            disabledRepeatSwitch(repeat);
            if (controller != null)
                controller.endLoopMode();
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
                resetButton.setEnabled(false);
                initTextFields(0, 0);
                LoopPreferences.setEnabled(loop);
                if (controller != null)
                    controller.endLoopMode();
            } else {
                if (controller != null) {
                    Playable playable = controller.getMedia();
                    if (playable instanceof FeedMedia) {
                        FeedMedia feedMedia = (FeedMedia) playable;
                        long itemId = feedMedia.getId();
                        LoopPreferences.setFeedItemId(itemId);
                    }
                    LoopPreferences.setEnabled(loop);
                    controller.startLoopMode();
                    initTextFields(controller.getPosition(), controller.getDuration());
                }
                repeatSectionCheckbox.setEnabled(true);
                repeatSectionCheckbox.setChecked(true);
                repeatEpisodeCheckbox.setChecked(false);
                startButton.setEnabled(true);
                endButton.setEnabled(true);
                startField.setEnabled(true);
                endField.setEnabled(true);
                resetButton.setEnabled(true);
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
        resetButton.setEnabled(enabled);
    }

    private void initTextFields(int startTime, int endTime) {
        String startTimeText = Converter.getDurationStringLong(startTime);
        String endTimeText = Converter.getDurationStringLong(endTime);
        startField.setText(startTimeText);
        endField.setText(endTimeText);
        LoopPreferences.setStart(startTime);
        LoopPreferences.setEnd(endTime);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startField.setText(Converter.getDurationStringLong(LoopPreferences.getStart()));
        endField.setText(Converter.getDurationStringLong(LoopPreferences.getEnd()));
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

        resetButton.setOnClickListener(v -> {
            LoopPreferences.setStart(0);
            LoopPreferences.setEnd(controller.getDuration());
            startField.setText(Converter.getDurationStringLong(0));
            endField.setText(Converter.getDurationStringLong(controller.getDuration()));
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Fragment destroyed");
    }

    @Override
    public void onStart() {
        super.onStart();
        controller = new PlaybackController(getActivity()) {
            @Override
            public void loadMediaInfo() {
            }
        };
        controller.init();
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
        if (key.equals(LoopPreferences.PREF_KEY_LOOP_ENABLED)) {
            setLoopModeView(repeatEnabled, LoopPreferences.isEnabled());
        }
    }
}
