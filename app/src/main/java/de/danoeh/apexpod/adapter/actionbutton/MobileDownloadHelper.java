package de.danoeh.apexpod.adapter.actionbutton;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.dialog.DownloadRequestErrorDialogCreator;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.DownloadRequestException;
import de.danoeh.apexpod.core.storage.DownloadRequester;

class MobileDownloadHelper {
    private static long addToQueueTimestamp;
    private static long allowMobileDownloadTimestamp;
    private static final int TEN_MINUTES_IN_MILLIS = 10 * 60 * 1000;

    static boolean userChoseAddToQueue() {
        return System.currentTimeMillis() - addToQueueTimestamp < TEN_MINUTES_IN_MILLIS;
    }

    static boolean userAllowedMobileDownloads() {
        return System.currentTimeMillis() - allowMobileDownloadTimestamp < TEN_MINUTES_IN_MILLIS;
    }

    static void confirmMobileDownload(final Context context, final FeedItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.confirm_mobile_download_dialog_title)
                .setMessage(R.string.confirm_mobile_download_dialog_message)
                .setPositiveButton(context.getText(R.string.confirm_mobile_download_dialog_enable_temporarily),
                        (dialog, which) -> downloadFeedItems(context, item));
        if (!DBReader.getQueueIDList().contains(item.getId())) {
            builder.setMessage(R.string.confirm_mobile_download_dialog_message_not_in_queue)
                    .setNeutralButton(R.string.confirm_mobile_download_dialog_only_add_to_queue,
                            (dialog, which) -> addToQueue(context, item));
        }
        builder.show();
    }

    private static void addToQueue(Context context, FeedItem item) {
        addToQueueTimestamp = System.currentTimeMillis();
        DBWriter.addQueueItem(context, item);
    }

    private static void downloadFeedItems(Context context, FeedItem item) {
        allowMobileDownloadTimestamp = System.currentTimeMillis();
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