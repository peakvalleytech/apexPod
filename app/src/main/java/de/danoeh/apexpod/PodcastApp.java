package de.danoeh.apexpod;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.MaterialModule;

import org.greenrobot.eventbus.EventBus;

import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.activity.SplashActivity;
import de.danoeh.apexpod.ads.AppOpenAdManager;
import de.danoeh.apexpod.core.ApCoreEventBusIndex;
import de.danoeh.apexpod.core.BuildConfig;
import de.danoeh.apexpod.core.ClientConfig;
import de.danoeh.apexpod.util.RxJavaErrorHandlerSetup;
import de.danoeh.apexpod.spa.SPAUtil;

/** Main application class. */
public class PodcastApp extends Application
        implements
            Application.ActivityLifecycleCallbacks,
        LifecycleObserver
{
    /** Interface definition for a callback to be invoked when an app open ad is complete. */
    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }
    // make sure that ClientConfigurator executes its static code
    static {
        try {
            Class.forName("de.danoeh.apexpod.config.ClientConfigurator");
        } catch (Exception e) {
            throw new RuntimeException("ClientConfigurator not found", e);
        }
    }

    private static PodcastApp singleton;

    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity;

    public static PodcastApp getInstance() {
        return singleton;
    }
    public FirebaseAnalytics firebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.logEvent("regiestering_lifecycle_callbacks", null);
        this.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        MobileAds.initialize(this, initializationStatus -> {
            firebaseAnalytics.logEvent("initialized_mobile_ads", null);
        });

//        appOpenAdManager = new AppOpenAdManager();
        RxJavaErrorHandlerSetup.setupRxJavaErrorHandler();

        if (BuildConfig.DEBUG) {
            firebaseAnalytics.logEvent("setting_vmPolicy", null);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .penaltyDropBox()
                    .detectActivityLeaks()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects();
            StrictMode.setVmPolicy(builder.build());
        }

        singleton = this;
        firebaseAnalytics.logEvent("init_client_config", null);
        ClientConfig.initialize(this);

        Iconify.with(new FontAwesomeModule());
        Iconify.with(new MaterialModule());

        SPAUtil.sendSPAppsQueryFeedsIntent(this);
        firebaseAnalytics.logEvent("init_event_bus", null);
        EventBus.builder()
                .addIndex(new ApEventBusIndex())
                .addIndex(new ApCoreEventBusIndex())
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .installDefaultEventBus();
    }

    public static void forceRestart() {
        Intent intent = new Intent(getInstance(), SplashActivity.class);
        ComponentName cn = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(cn);
        getInstance().startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
// Updating the currentActivity only when an ad is not showing.
//        if (appOpenAdManager != null &&
//                !appOpenAdManager.isShowingAd && activity.getLocalClassName().equals("de.danoeh.apexpod.activity.SplashActivity")) {
//            currentActivity = activity;
//        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
//        if (currentActivity != null && appOpenAdManager != null)
//            appOpenAdManager.showAdIfAvailable(
//                currentActivity,
//
//                new OnShowAdCompleteListener() {
//                    @Override
//                    public void onShowAdComplete() {
//                        // Empty because the user will go back to the activity that shows the ad.
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    }
//                });
    }
}
