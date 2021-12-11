package de.danoeh.apexpod.adapter.actionbutton;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.ArrayList;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.dialog.DownloadRequestErrorDialogCreator;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.core.preferences.UsageStatistics;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.DownloadRequestException;
import de.danoeh.apexpod.core.storage.DownloadRequester;
import de.danoeh.apexpod.core.util.NetworkUtils;

public class DownloadActionButton extends ItemActionButton {
    private boolean isInQueue;

    public DownloadActionButton(FeedItem item) {
        super(item);
        this.isInQueue = item.isTagged(FeedItem.TAG_QUEUE);;
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.download_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_download;
    }

    @Override
    public int getVisibility() {
        return item.getFeed().isLocalFeed() ? View.INVISIBLE : View.VISIBLE;
    }

    @Override
    public void onClick(Context context) {
        final FeedMedia media = item.getMedia();
        if (media == null || shouldNotDownload(media)) {
            return;
        }

        UsageStatistics.logAction(UsageStatistics.ACTION_DOWNLOAD);

        if (NetworkUtils.isEpisodeDownloadAllowed() || MobileDownloadHelper.userAllowedMobileDownloads()) {
            downloadEpisode(context);
        } else if (MobileDownloadHelper.userChoseAddToQueue() && !isInQueue) {
            addEpisodeToQueue(context);
        } else {
            MobileDownloadHelper.confirmMobileDownload(context, item);
        }
    }

    private boolean shouldNotDownload(@NonNull FeedMedia media) {
        boolean isDownloading = DownloadRequester.getInstance().isDownloadingFile(media);
        return isDownloading || media.isDownloaded();
    }

    private void addEpisodeToQueue(Context context) {
        DBWriter.addQueueItem(context, item);
        Toast.makeText(context, R.string.added_to_queue_label, Toast.LENGTH_SHORT).show();
    }

    private void downloadEpisode(Context context) {
        try {
            ArrayList<FeedItem> feedItems = new ArrayList<>();
            feedItems.add(item);
            DownloadRequester.getInstance().downloadMedia(context, true, feedItems);
        } catch (DownloadRequestException e) {
            e.printStackTrace();
            DownloadRequestErrorDialogCreator.newRequestErrorDialog(context, e.getMessage());
        }
    }
}
