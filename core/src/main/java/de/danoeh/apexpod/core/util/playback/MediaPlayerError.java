package de.danoeh.apexpod.core.util.playback;

import android.content.Context;
import android.media.MediaPlayer;
import com.google.android.exoplayer2.ExoPlaybackException;
import de.danoeh.apexpod.core.R;
import de.danoeh.apexpod.core.service.playback.player.ApexPlayer;

/** Utility class for MediaPlayer errors. */
public class MediaPlayerError {
    private MediaPlayerError(){}

    /** Get a human-readable string for a specific error code. */
    public static String getErrorString(Context context, int code) {
        int resId;
        switch (code) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                resId = R.string.playback_error_server_died;
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED: // fall-through
            case ApexPlayer.ERROR_CODE_OFFSET + ExoPlaybackException.TYPE_RENDERER:
                resId = R.string.playback_error_unsupported;
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                resId = R.string.playback_error_timeout;
                break;
            case ApexPlayer.ERROR_CODE_OFFSET + ExoPlaybackException.TYPE_SOURCE:
                resId = R.string.playback_error_source;
                break;
            default:
                resId = R.string.playback_error_unknown;
                break;
        }
        return context.getString(resId) + " (" + code + ")";
    }
}
