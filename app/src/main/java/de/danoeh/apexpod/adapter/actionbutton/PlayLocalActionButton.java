package de.danoeh.apexpod.adapter.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.model.playback.MediaType;
import de.danoeh.apexpod.core.service.playback.PlaybackService;
import de.danoeh.apexpod.core.util.playback.PlaybackServiceStarter;

public class PlayLocalActionButton extends ItemActionButton {

    public PlayLocalActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.play_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_play_24dp;
    }

    @Override
    public void onClick(Context context, long autoPlayMode) {
        final FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }

        new PlaybackServiceStarter(context, media)
                .callEvenIfRunning(true)
                .startWhenPrepared(true)
                .shouldStream(true)
                .start();

        if (media.getMediaType() == MediaType.VIDEO) {
            context.startActivity(PlaybackService.getPlayerActivityIntent(context, media));
        }
    }
}
