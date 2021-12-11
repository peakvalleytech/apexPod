package de.danoeh.apexpod.adapter.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.DownloadRequester;

public class CancelDownloadActionButton extends ItemActionButton {

    public CancelDownloadActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.cancel_download_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_cancel;
    }

    @Override
    public void onClick(Context context) {
        FeedMedia media = item.getMedia();
        DownloadRequester.getInstance().cancelDownload(context, media);
        if (UserPreferences.isEnableAutodownload()) {
            item.setAutoDownload(false);
            DBWriter.setFeedItem(item);
        }
    }
}
