package de.danoeh.apexpod.core.service.playback.player;

import android.content.Context;
import androidx.annotation.StringRes;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import de.danoeh.apexpod.core.service.playback.PlaybackService;
import de.danoeh.apexpod.core.service.playback.player.LocalPlaybackServiceMediaPlayer;

/**
 * Class intended to work along PlaybackService and provide support for different flavors.
 */
public class PlaybackServiceFlavorHelper {

    private final PlaybackService.FlavorHelperCallback callback;

    public PlaybackServiceFlavorHelper(Context context, PlaybackService.FlavorHelperCallback callback) {
        this.callback = callback;
    }

    public void initializeMediaPlayer(Context context) {
        callback.setMediaPlayer(new LocalPlaybackServiceMediaPlayer(context, callback.getMediaPlayerCallback()));
    }

    public void removeCastConsumer() {
        // no-op
    }

    public boolean castDisconnect(boolean castDisconnect) {
        return false;
    }

    public boolean onMediaPlayerInfo(Context context, int code, @StringRes int resourceId) {
        return false;
    }

    public void registerWifiBroadcastReceiver() {
        // no-op
    }

    public void unregisterWifiBroadcastReceiver() {
        // no-op
    }

    public boolean onSharedPreference(String key) {
        return false;
    }

    public void sessionStateAddActionForWear(PlaybackStateCompat.Builder sessionState, String actionName, CharSequence name, int icon) {
        // no-op
    }

    public void mediaSessionSetExtraForWear(MediaSessionCompat mediaSession) {
        // no-op
    }
}
