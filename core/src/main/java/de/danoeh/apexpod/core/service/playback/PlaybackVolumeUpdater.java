package de.danoeh.apexpod.core.service.playback;

import de.danoeh.apexpod.core.service.playback.player.BaseMediaPlayer;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.model.feed.FeedPreferences;
import de.danoeh.apexpod.model.feed.VolumeAdaptionSetting;
import de.danoeh.apexpod.model.playback.Playable;

class PlaybackVolumeUpdater {

    public void updateVolumeIfNecessary(BaseMediaPlayer mediaPlayer, long feedId,
                                        VolumeAdaptionSetting volumeAdaptionSetting) {
        Playable playable = mediaPlayer.getPlayable();

        if (playable instanceof FeedMedia) {
            updateFeedMediaVolumeIfNecessary(mediaPlayer, feedId, volumeAdaptionSetting, (FeedMedia) playable);
        }
    }

    private void updateFeedMediaVolumeIfNecessary(BaseMediaPlayer mediaPlayer, long feedId,
                                                  VolumeAdaptionSetting volumeAdaptionSetting, FeedMedia feedMedia) {
        if (feedMedia.getItem().getFeed().getId() == feedId) {
            FeedPreferences preferences = feedMedia.getItem().getFeed().getPreferences();
            preferences.setVolumeAdaptionSetting(volumeAdaptionSetting);

            if (mediaPlayer.getPlayerStatus() == PlayerStatus.PLAYING) {
                forceUpdateVolume(mediaPlayer);
            }
        }
    }

    private void forceUpdateVolume(BaseMediaPlayer mediaPlayer) {
        mediaPlayer.pause(false, false);
        mediaPlayer.resume();
    }

}
