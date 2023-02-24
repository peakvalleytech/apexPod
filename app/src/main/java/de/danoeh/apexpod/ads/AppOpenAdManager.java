package de.danoeh.apexpod.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import de.danoeh.apexpod.PodcastApp;

/**
 * Inner class that loads and shows app open ads.
 */
public class AppOpenAdManager {
    private static final String LOG_TAG = "AppOpenAdManager";
    private static final String AD_UNIT_ID = "ca-app-pub-3142101706002267/5748126482";

    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    public boolean isShowingAd = false;

    /**
     * Constructor.
     */
    public AppOpenAdManager() {
    }

    /**
     * Request an ad.
     */
    private void loadAd(Context context) {
        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable()) {
            return;
        }

        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                context, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        // Called when an app open ad has loaded.
                        Log.d(LOG_TAG, "Ad was loaded.");
                        appOpenAd = ad;
                        isLoadingAd = false;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Called when an app open ad has failed to load.
                        Log.d(LOG_TAG, loadAdError.getMessage());
                        isLoadingAd = false;
                    }
                });
    }

    /**
     * Check if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null;
    }
    /**
     * Shows the ad if one isn't already showing.
     */
    public void showAdIfAvailable(
            @NonNull final Activity activity,
            @NonNull PodcastApp.OnShowAdCompleteListener onShowAdCompleteListener) {

        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Log.d(LOG_TAG, "The app open ad is already showing.");
            return;
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.");
            onShowAdCompleteListener.onShowAdComplete();
            loadAd(activity);
            return;
        }

        appOpenAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        // Set the reference to null so isAdAvailable() returns false.
                        Log.d(LOG_TAG, "Ad dismissed fullscreen content.");
                        appOpenAd = null;
                        isShowingAd = false;

                        onShowAdCompleteListener.onShowAdComplete();
                        loadAd(activity);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when fullscreen content failed to show.
                        // Set the reference to null so isAdAvailable() returns false.
                        Log.d(LOG_TAG, adError.getMessage());
                        appOpenAd = null;
                        isShowingAd = false;

                        onShowAdCompleteListener.onShowAdComplete();
                        loadAd(activity);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        Log.d(LOG_TAG, "Ad showed fullscreen content.");
                    }
                });
        isShowingAd = true;
        appOpenAd.show(activity);
    }

}
