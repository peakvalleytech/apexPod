package de.danoeh.apexpod.core;

import android.content.Context;
import android.util.Log;
import de.danoeh.apexpod.core.cast.CastManager;
import de.danoeh.apexpod.core.preferences.LoopPreferences;
import de.danoeh.apexpod.core.preferences.PlaybackPreferences;
import de.danoeh.apexpod.core.preferences.SleepTimerPreferences;
import de.danoeh.apexpod.core.preferences.UsageStatistics;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.service.download.AntennapodHttpClient;
import de.danoeh.apexpod.core.storage.PodDBAdapter;
import de.danoeh.apexpod.core.util.NetworkUtils;
import de.danoeh.apexpod.core.util.gui.NotificationUtils;
import de.danoeh.apexpod.net.ssl.SslProviderInstaller;

import java.io.File;

/**
 * Stores callbacks for core classes like Services, DB classes etc. and other configuration variables.
 * Apps using the core module of AntennaPod should register implementations of all interfaces here.
 */
public class ClientConfig {
    private static final String TAG = "ClientConfig";

    private ClientConfig(){}

    /**
     * Should be used when setting User-Agent header for HTTP-requests.
     */
    public static String USER_AGENT;

    public static ApplicationCallbacks applicationCallbacks;

    public static DownloadServiceCallbacks downloadServiceCallbacks;

    public static CastCallbacks castCallbacks;

    private static boolean initialized = false;

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        PodDBAdapter.init(context);
        UserPreferences.init(context);
        LoopPreferences.init(context);
        UsageStatistics.init(context);
        PlaybackPreferences.init(context);
        SslProviderInstaller.install(context);
        NetworkUtils.init(context);
        // Don't initialize Cast-related logic unless it is enabled, to avoid the unnecessary
        // Google Play Service usage.
        // Down side: when the user decides to enable casting, AntennaPod needs to be restarted
        // for it to take effect.
        if (UserPreferences.isCastEnabled()) {
            CastManager.init(context);
        } else {
            Log.v(TAG, "Cast is disabled. All Cast-related initialization will be skipped.");
        }
        AntennapodHttpClient.setCacheDirectory(new File(context.getCacheDir(), "okhttp"));
        SleepTimerPreferences.init(context);
        NotificationUtils.createChannels(context);
        initialized = true;
    }
}
