package de.danoeh.antennapod.core.util;

import android.content.Context;
import android.util.Log;

import org.awaitility.core.ConditionTimeoutException;

import java.util.concurrent.TimeUnit;

import de.danoeh.antennapod.core.storage.DBTasks;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;

public class FeedUpdateUtils {
    private static final String TAG = "FeedUpdateUtils";

    private FeedUpdateUtils() {}

    public static void startAutoUpdate(Context context, Runnable callback) {
        try {
            with().pollInterval(1, TimeUnit.SECONDS)
                    .await()
                    .atMost(10, TimeUnit.SECONDS)
                    .until(() -> NetworkUtils.networkAvailable() && NetworkUtils.isDownloadAllowed());
            DBTasks.refreshAllFeeds(context, null, callback);
        } catch (ConditionTimeoutException ignore) {
            Log.d(TAG, "Blocking automatic update: no wifi available / no mobile updates allowed");
        }
    }

}
