package de.danoeh.apexpod.adapter.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.core.util.FeedItemUtil;
import de.danoeh.apexpod.core.util.IntentUtils;

import static de.danoeh.apexpod.core.service.playback.PlaybackService.ACTION_PAUSE_PLAY_CURRENT_EPISODE;

public class PauseActionButton extends ItemActionButton {

    public PauseActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.pause_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_pause;
    }

    @Override
    public void onClick(Context context, long plid) {
        FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }

        if (FeedItemUtil.isCurrentlyPlaying(media)) {
            IntentUtils.sendLocalBroadcast(context, ACTION_PAUSE_PLAY_CURRENT_EPISODE);
        }
    }
}
