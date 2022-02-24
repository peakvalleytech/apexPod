package de.danoeh.apexpod.core.service.playback;

import android.util.Log;

import androidx.annotation.NonNull;

import de.danoeh.apexpod.core.service.playback.player.BaseMediaPlayer;
import de.danoeh.apexpod.core.widget.WidgetUpdater;
import de.danoeh.apexpod.model.playback.Playable;

public class PlaybackTaskManagerCallback implements PlaybackServiceTaskManager.TaskManagerCallback {
    private static final String TAG = "PlaybackTaskMgrCallb";
    private PlaybackService playbackService;

    public PlaybackTaskManagerCallback(@NonNull PlaybackService playbackService) {
        this.playbackService = playbackService;
    }

    @Override
        public void positionSaverTick() {
        playbackService.saveCurrentPosition(true, null, BaseMediaPlayer.INVALID_TIME);
        }

        @Override
        public void onSleepTimerAlmostExpired(long timeLeft) {
            final float[] multiplicators = {0.1f, 0.2f, 0.3f, 0.3f, 0.3f, 0.4f, 0.4f, 0.4f, 0.6f, 0.8f};
            float multiplicator = multiplicators[Math.max(0, (int) timeLeft / 1000)];
            Log.d(TAG, "onSleepTimerAlmostExpired: " + multiplicator);
            playbackService.mediaPlayer.setVolume(multiplicator, multiplicator);
        }

        @Override
        public void onSleepTimerExpired() {
            playbackService.mediaPlayer.pause(true, true);
            playbackService.mediaPlayer.setVolume(1.0f, 1.0f);
            playbackService.sendNotificationBroadcast(playbackService.NOTIFICATION_TYPE_SLEEPTIMER_UPDATE, 0);
        }

        @Override
        public void onSleepTimerReset() {
            playbackService.mediaPlayer.setVolume(1.0f, 1.0f);
        }

        @Override
        public WidgetUpdater.WidgetState requestWidgetState() {
            return new WidgetUpdater.WidgetState(playbackService.getPlayable(), playbackService.getStatus(),
                    playbackService.getCurrentPosition(), playbackService.getDuration(), playbackService.getCurrentPlaybackSpeed(), playbackService.isCasting());
        }

        @Override
        public void onChapterLoaded(Playable media) {
            playbackService.sendNotificationBroadcast(playbackService.NOTIFICATION_TYPE_RELOAD, 0);
        }
}
