package de.danoeh.apexpod.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.util.playback.PlaybackController;
import de.danoeh.apexpod.view.PlaybackSpeedSeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.List;
import java.util.Locale;

public class PlaybackControlsDialog extends DialogFragment {
    private PlaybackController controller;
    private AlertDialog dialog;
    private PlaybackSpeedSeekBar speedSeekBar;
    private TextView txtvPlaybackSpeed;

    public static PlaybackControlsDialog newInstance() {
        Bundle arguments = new Bundle();
        PlaybackControlsDialog dialog = new PlaybackControlsDialog();
        dialog.setArguments(arguments);
        return dialog;
    }

    public PlaybackControlsDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onStart() {
        super.onStart();
        controller = new PlaybackController(getActivity()) {
            @Override
            public void loadMediaInfo() {
                setupUi();
                setupAudioTracks();
                updateSpeed();
            }

            @Override
            public void onPlaybackSpeedChange() {
                updateSpeed();
            }
        };
        controller.init();
        setupUi();
    }

    @Override
    public void onStop() {
        super.onStop();
        controller.release();
        controller = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.audio_controls)
                .setView(R.layout.audio_controls)
                .setPositiveButton(R.string.close_label, null).create();
        return dialog;
    }

    private void setupUi() {
        txtvPlaybackSpeed = dialog.findViewById(R.id.txtvPlaybackSpeed);
        speedSeekBar = dialog.findViewById(R.id.speed_seek_bar);
        speedSeekBar.setProgressChangedListener(speed -> {
            if (controller != null) {
                controller.setPlaybackSpeed(speed);
                updateSpeed();
            }
        });
        updateSpeed();

        final CheckBox stereoToMono = dialog.findViewById(R.id.stereo_to_mono);
        stereoToMono.setChecked(UserPreferences.stereoToMono());
        final CheckBox repeatEpisode = dialog.findViewById(R.id.repeat_episode);
        repeatEpisode.setChecked(UserPreferences.getShouldRepeatEpisode());
        if (controller != null && !controller.canDownmix()) {
            stereoToMono.setEnabled(false);
            String sonicOnly = getString(R.string.sonic_only);
            stereoToMono.setText(getString(R.string.stereo_to_mono) + " [" + sonicOnly + "]");
        }

        final CheckBox skipSilence = dialog.findViewById(R.id.skipSilence);
        skipSilence.setChecked(UserPreferences.isSkipSilence());
        if (!UserPreferences.useExoplayer()) {
            skipSilence.setEnabled(false);
            String exoplayerOnly = getString(R.string.exoplayer_only);
            skipSilence.setText(getString(R.string.pref_skip_silence_title) + " [" + exoplayerOnly + "]");
        }
        skipSilence.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserPreferences.setSkipSilence(isChecked);
            controller.setSkipSilence(isChecked);
        });
        stereoToMono.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserPreferences.stereoToMono(isChecked);
            if (controller != null) {
                controller.setDownmix(isChecked);
            }
        });
        repeatEpisode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserPreferences.setShouldRepeatEpisode(isChecked);
        });
    }

    private void updateSpeed() {
        if (controller != null) {
            txtvPlaybackSpeed.setText(String.format(
                    Locale.getDefault(), "%.2fx", controller.getCurrentPlaybackSpeedMultiplier()));
            speedSeekBar.updateSpeed(controller.getCurrentPlaybackSpeedMultiplier());
        }
    }

    private void setupAudioTracks() {
        List<String> audioTracks = controller.getAudioTracks();
        int selectedAudioTrack = controller.getSelectedAudioTrack();
        final Button butAudioTracks = dialog.findViewById(R.id.audio_tracks);
        if (audioTracks.size() < 2 || selectedAudioTrack < 0) {
            butAudioTracks.setVisibility(View.GONE);
            return;
        }

        butAudioTracks.setVisibility(View.VISIBLE);
        butAudioTracks.setText(audioTracks.get(selectedAudioTrack));
        butAudioTracks.setOnClickListener(v -> {
            controller.setAudioTrack((selectedAudioTrack + 1) % audioTracks.size());
            new Handler(Looper.getMainLooper()).postDelayed(this::setupAudioTracks, 500);
        });
    }
}