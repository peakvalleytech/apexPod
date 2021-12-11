package de.danoeh.apexpod.adapter.actionbutton;

import android.content.Context;
import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.core.storage.DBWriter;

public class DeleteActionButton extends ItemActionButton {

    public DeleteActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.delete_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_delete;
    }

    @Override
    public void onClick(Context context, long autoPlayMode) {
        final FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }
        DBWriter.deleteFeedMediaOfItem(context, media.getId());
    }

    @Override
    public int getVisibility() {
        return (item.getMedia() != null && item.getMedia().isDownloaded()) ? View.VISIBLE : View.INVISIBLE;
    }
}
