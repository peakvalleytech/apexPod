package de.danoeh.apexpod.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LightingColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.discovery.FeedDownloader;
import de.danoeh.apexpod.adapter.FeedItemlistDescriptionAdapter;
import de.danoeh.apexpod.dialog.DownloadRequestErrorDialogCreator;
import de.danoeh.apexpod.core.event.DownloadEvent;
import de.danoeh.apexpod.core.event.FeedListUpdateEvent;
import de.danoeh.apexpod.core.event.PlayerStatusEvent;
import de.danoeh.apexpod.core.glide.ApGlideSettings;
import de.danoeh.apexpod.core.glide.FastBlurTransformation;
import de.danoeh.apexpod.core.preferences.PlaybackPreferences;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.service.download.DownloadStatus;
import de.danoeh.apexpod.core.service.download.Downloader;
import de.danoeh.apexpod.core.service.playback.PlaybackService;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.DownloadRequestException;
import de.danoeh.apexpod.core.storage.DownloadRequester;
import de.danoeh.apexpod.parser.feed.FeedHandler;
import de.danoeh.apexpod.parser.feed.FeedHandlerResult;
import de.danoeh.apexpod.core.util.DownloadError;
import de.danoeh.apexpod.core.util.IntentUtils;
import de.danoeh.apexpod.core.util.StorageUtils;
import de.danoeh.apexpod.core.util.syndication.FeedDiscoverer;
import de.danoeh.apexpod.core.util.syndication.HtmlToPlainText;
import de.danoeh.apexpod.databinding.OnlinefeedviewActivityBinding;
import de.danoeh.apexpod.dialog.AuthenticationDialog;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedPreferences;
import de.danoeh.apexpod.model.playback.RemoteMedia;
import de.danoeh.apexpod.parser.feed.UnsupportedFeedtypeException;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Downloads a feed from a feed URL and parses it. Subclasses can display the
 * feed object that was parsed. This activity MUST be started with a given URL
 * or an Exception will be thrown.
 * <p/>
 * If the feed cannot be downloaded or parsed, an error dialog will be displayed
 * and the activity will finish as soon as the error dialog is closed.
 */
public class OnlineFeedViewActivity extends AppCompatActivity {

    public static final String ARG_FEEDURL = "arg.feedurl";
    public static final String ARG_IS_SUBSCRIBED = "arg.isSubscribed";
    public static final String ARG_SUBSCRIBED_FEED_ID = "arg.subscribedFeedId";
    // Optional argument: specify a title for the actionbar.
    private static final int RESULT_ERROR = 2;
    private static final String TAG = "OnlineFeedViewActivity";
    private static final String PREFS = "OnlineFeedViewActivityPreferences";
    private static final String PREF_LAST_AUTO_DOWNLOAD = "lastAutoDownload";

    private volatile List<Feed> feeds;
    private Feed feed;
    /*
        Id passed using intent. Used so that already subscribed feeds can be opened.
     */
    private Long subscribedFeedId = 0L;
    private String selectedDownloadUrl;
    private Downloader downloader;

    private boolean isPaused;

    private Dialog dialog;

    private Disposable parser;
    private Disposable updater;

    private OnlinefeedviewActivityBinding viewBinding;

    FeedDownloader feedDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UserPreferences.getTranslucentTheme());
        super.onCreate(savedInstanceState);
        StorageUtils.checkStorageAvailability(this);

        viewBinding = OnlinefeedviewActivityBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        viewBinding.transparentBackground.setOnClickListener(v -> finish());
        viewBinding.card.setOnClickListener(null);

        String feedUrl = null;
        Intent intent = getIntent();
        if (intent.hasExtra(ARG_FEEDURL)) {
            feedUrl = intent.getStringExtra(ARG_FEEDURL);
        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_SEND)
                || TextUtils.equals(intent.getAction(), Intent.ACTION_VIEW)) {
            feedUrl = TextUtils.equals(intent.getAction(), Intent.ACTION_SEND)
                    ? intent.getStringExtra(Intent.EXTRA_TEXT) : intent.getDataString();
        }

        if (intent.hasExtra(ARG_SUBSCRIBED_FEED_ID)) {
            subscribedFeedId = intent.getLongExtra(ARG_SUBSCRIBED_FEED_ID, 0L);
        }



        if (feedUrl == null) {
            Log.e(TAG, "feedUrl is null.");
            showNoPodcastFoundError();
        } else {
            Log.d(TAG, "Activity was started with url " + feedUrl);
            setLoadingLayout();
            // Remove subscribeonandroid.com from feed URL in order to subscribe to the actual feed URL
            if (feedUrl.contains("subscribeonandroid.com")) {
                feedUrl = feedUrl.replaceFirst("((www.)?(subscribeonandroid.com/))", "");
            }
            String username = null, password = null;
            if (!isAuthenticated(savedInstanceState)) {
                username = savedInstanceState.getString("username");
                password = savedInstanceState.getString("password");
            }
            feedDownloader = new FeedDownloader(this);
            feedDownloader.lookupUrl(
                    feedUrl,
                    username,
                    password,
                    (result) -> {
                        checkDownloadResult(result);
                        return null;
                    },() -> {
                        showNoPodcastFoundError();
                        return null;
                    }
            );
        }
    }

    private boolean isAuthenticated(Bundle savedInstanceState) {
        return savedInstanceState == null;
    }
    private void showNoPodcastFoundError() {
        runOnUiThread(() -> new AlertDialog.Builder(OnlineFeedViewActivity.this)
                .setNeutralButton(android.R.string.ok, (dialog, which) -> finish())
                .setTitle(R.string.error_label)
                .setMessage(R.string.null_value_podcast_error)
                .setOnDismissListener(dialog1 -> {
                    setResult(RESULT_ERROR);
                    finish();
                })
                .show());
    }

    /**
     * Displays a progress indicator.
     */
    private void setLoadingLayout() {
        viewBinding.progressBar.setVisibility(View.VISIBLE);
        viewBinding.feedDisplayContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isPaused = false;
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isPaused = true;
        EventBus.getDefault().unregister(this);
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(updater != null) {
            updater.dispose();
        }
        if(parser != null) {
            parser.dispose();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (feed != null && feed.getPreferences() != null) {
            outState.putString("username", feed.getPreferences().getUsername());
            outState.putString("password", feed.getPreferences().getPassword());
        }
    }

    private void resetIntent(String url) {
        Intent intent = new Intent();
        intent.putExtra(ARG_FEEDURL, url);
        setIntent(intent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent destIntent = new Intent(this, MainActivity.class);
            if (NavUtils.shouldUpRecreateTask(this, destIntent)) {
                startActivity(destIntent);
            } else {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkDownloadResult(@NonNull DownloadStatus status) {
        if (status.isCancelled()) {
            return;
        }
        if (status.isSuccessful()) {
            parseFeed();
        } else if (status.getReason() == DownloadError.ERROR_UNAUTHORIZED) {
            if (!isFinishing() && !isPaused) {
                dialog = new FeedViewAuthenticationDialog(OnlineFeedViewActivity.this,
                        R.string.authentication_notification_title,
                        downloader.getDownloadRequest().getSource()).create();
                dialog.show();
            }
        } else {
            showErrorDialog(status.getReason().getErrorString(OnlineFeedViewActivity.this), status.getReasonDetailed());
        }
    }

    @Subscribe
    public void onFeedListChanged(FeedListUpdateEvent event) {
        updater = Observable.fromCallable(DBReader::getFeedList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        feeds -> {
                            this.feeds = feeds;
                            updateSubscribeButton(feed);
                        }, error -> Log.e(TAG, Log.getStackTraceString(error))
                );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        updateSubscribeButton(feed);
    }

    private void parseFeed() {
        this.feed = feedDownloader.getFeed();
        if (this.feed == null || (feed.getFile_url() == null && feed.isDownloaded())) {
            throw new IllegalStateException("feed must be non-null and downloaded when parseFeed is called");
        }
        Log.d(TAG, "Parsing feed");

        parser = Maybe.fromCallable(this::doParseFeed)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableMaybeObserver<FeedHandlerResult>() {
                    @Override
                    public void onSuccess(@NonNull FeedHandlerResult result) {
                        showFeedInformation(result.feed, result.alternateFeedUrls);
                    }

                    @Override
                    public void onComplete() {
                        // Ignore null result: We showed the discovery dialog.
                    }

                    @Override
                    public void onError(@NonNull Throwable error) {
                        showErrorDialog(error.getMessage(), "");
                        Log.d(TAG, "Feed parser exception: " + Log.getStackTraceString(error));
                    }
                });
    }

    /**
     * Try to parse the feed.
     * @return  The FeedHandlerResult if successful.
     *          Null if unsuccessful but we started another attempt.
     * @throws Exception If unsuccessful but we do not know a resolution.
     */
    @Nullable
    private FeedHandlerResult doParseFeed() throws Exception {
        FeedHandler handler = new FeedHandler();
        try {
            return handler.parseFeed(feed);
        } catch (UnsupportedFeedtypeException e) {
            Log.d(TAG, "Unsupported feed type detected");
            if ("html".equalsIgnoreCase(e.getRootElement())) {
                boolean dialogShown = showFeedDiscoveryDialog(new File(feed.getFile_url()), feed.getDownload_url());
                if (dialogShown) {
                    return null; // Should not display an error message
                } else {
                    throw new UnsupportedFeedtypeException(getString(R.string.download_error_unsupported_type_html));
                }
            } else {
                throw e;
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw e;
        } finally {
            boolean rc = new File(feed.getFile_url()).delete();
            Log.d(TAG, "Deleted feed source file. Result: " + rc);
        }
    }

    /**
     * Called when feed parsed successfully.
     * This method is executed on the GUI thread.
     */
    private void showFeedInformation(final Feed feed, Map<String, String> alternateFeedUrls) {
        viewBinding.progressBar.setVisibility(View.GONE);
        viewBinding.feedDisplayContainer.setVisibility(View.VISIBLE);
        this.feed = feed;
        this.selectedDownloadUrl = feed.getDownload_url();

        viewBinding.backgroundImage.setColorFilter(new LightingColorFilter(0xff828282, 0x000000));

        View header = View.inflate(this, R.layout.onlinefeedview_header, null);

        viewBinding.listView.addHeaderView(header);
        viewBinding.listView.setSelector(android.R.color.transparent);
        viewBinding.listView.setAdapter(new FeedItemlistDescriptionAdapter(this, 0, feed.getItems()));

        TextView description = header.findViewById(R.id.txtvDescription);

        if (StringUtils.isNotBlank(feed.getImageUrl())) {
            Glide.with(this)
                    .load(feed.getImageUrl())
                    .apply(new RequestOptions()
                        .placeholder(R.color.light_gray)
                        .error(R.color.light_gray)
                        .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                        .fitCenter()
                        .dontAnimate())
                    .into(viewBinding.coverImage);
            Glide.with(this)
                    .load(feed.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.image_readability_tint)
                            .error(R.color.image_readability_tint)
                            .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                            .transform(new FastBlurTransformation())
                            .dontAnimate())
                    .into(viewBinding.backgroundImage);
        }

        viewBinding.titleLabel.setText(feed.getTitle());
        viewBinding.authorLabel.setText(feed.getAuthor());
        description.setText(HtmlToPlainText.getPlainText(feed.getDescription()));

        viewBinding.subscribeButton.setOnClickListener(v -> {
            if (isSubscribed(feed)) {
                openFeed();
            } else {
                try {
                    DownloadRequester.getInstance().downloadFeed(this, feed);
                } catch (DownloadRequestException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    DownloadRequestErrorDialogCreator.newRequestErrorDialog(this, e.getMessage());
                }
                updateSubscribeButton(feed);
            }
        });

        /*CheckBox autoDownloadCheckBox = viewBinding.autoDownloadCheckBox;
        autoDownloadCheckBox.setVisibility(isSubscribed(feed) ? View.GONE : View.VISIBLE);
        autoDownloadCheckBox.setChecked(UserPreferences.isEnableAutodownload());

        viewBinding.autoDownloadCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Feed feedTemp = DBReader.getFeed(getFeedId(feed));
            FeedPreferences feedPreferences = feedTemp.getPreferences();
            feedPreferences.setAutoDownload(isChecked);
            DBWriter.setFeedPreferences(feedPreferences);

            SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_LAST_AUTO_DOWNLOAD, isChecked);
            editor.apply();
        });*/

        viewBinding.stopPreviewButton.setOnClickListener(v -> {
            PlaybackPreferences.writeNoMediaPlaying();
            IntentUtils.sendLocalBroadcast(this, PlaybackService.ACTION_SHUTDOWN_PLAYBACK_SERVICE);
        });

       /* if (UserPreferences.isEnableAutodownload()) {
            SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
            viewBinding.autoDownloadCheckBox.setChecked(preferences.getBoolean(PREF_LAST_AUTO_DOWNLOAD, true));
        }*/

        final int MAX_LINES_COLLAPSED = 10;
        description.setMaxLines(MAX_LINES_COLLAPSED);
        description.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && description.getMaxLines() > MAX_LINES_COLLAPSED) {
                description.setMaxLines(MAX_LINES_COLLAPSED);
            } else {
                description.setMaxLines(2000);
            }
        });

        if (alternateFeedUrls.isEmpty()) {
            viewBinding.alternateUrlsSpinner.setVisibility(View.GONE);
        } else {
            viewBinding.alternateUrlsSpinner.setVisibility(View.VISIBLE);

            final List<String> alternateUrlsList = new ArrayList<>();
            final List<String> alternateUrlsTitleList = new ArrayList<>();

            alternateUrlsList.add(feed.getDownload_url());
            alternateUrlsTitleList.add(feed.getTitle());


            alternateUrlsList.addAll(alternateFeedUrls.keySet());
            for (String url : alternateFeedUrls.keySet()) {
                alternateUrlsTitleList.add(alternateFeedUrls.get(url));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    R.layout.alternate_urls_item, alternateUrlsTitleList) {
                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    // reusing the old view causes a visual bug on Android <= 10
                    return super.getDropDownView(position, null, parent);
                }
            };

            adapter.setDropDownViewResource(R.layout.alternate_urls_dropdown_item);
            viewBinding.alternateUrlsSpinner.setAdapter(adapter);
            viewBinding.alternateUrlsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedDownloadUrl = alternateUrlsList.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        updateSubscribeButton(feed);
    }

    private void openFeed() {
        // feed.getId() is always 0, we have to retrieve the id from the feed list from
        // the database
        Intent intent = MainActivity.getIntentToOpenFeed(this, getFeedId(feed));
        intent.putExtra(MainActivity.EXTRA_STARTED_FROM_SEARCH,
                getIntent().getBooleanExtra(MainActivity.EXTRA_STARTED_FROM_SEARCH, false));
        finish();
        startActivity(intent);
    }

    private void updateSubscribeButton(Feed feed) {
        if (feed != null) {
            if (DownloadRequester.getInstance().isDownloadingFile(feed.getDownload_url())) {
                viewBinding.subscribeButton.setEnabled(false);
                viewBinding.subscribeButton.setText(R.string.subscribing_label);
            } else if (isSubscribed(feed)) {
                viewBinding.subscribeButton.setEnabled(true);
                viewBinding.subscribeButton.setText(R.string.open_podcast);
//                    openFeed();
            } else {
                viewBinding.subscribeButton.setEnabled(true);
                viewBinding.subscribeButton.setText(R.string.subscribe_label);
               /* if (UserPreferences.isEnableAutodownload()) {
                    viewBinding.autoDownloadCheckBox.setVisibility(View.VISIBLE);
                }*/
            }
        }
    }

    private boolean isSubscribed(Feed feed) {
        if (subscribedFeedId > 0)
            return true;
        if (feeds == null || feed == null) {
            return false;
        }
        for (Feed f : feeds) {
            if (f.getIdentifyingValue().equals(feed.getIdentifyingValue())) {
                return true;
            }
        }
        return false;
    }

    private long getFeedId(Feed feed) {
        if (subscribedFeedId == 0) {
            // Feeds is only non null when it activity recieves a feed changed event due to
            // subscribe button being press
            if (feeds != null) {
                for (Feed f : feeds) {
                    if (f.getIdentifyingValue().equals(feed.getIdentifyingValue())) {
                        return f.getId();
                    }
                }
            }
        }

        return subscribedFeedId;
    }

    @UiThread
    private void showErrorDialog(String errorMsg, String details) {
        if (!isFinishing() && !isPaused) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error_label);
            if (errorMsg != null) {
                String total = errorMsg + "\n\n" + details;
                SpannableString errorMessage = new SpannableString(total);
                errorMessage.setSpan(new ForegroundColorSpan(0x88888888),
                        errorMsg.length(), total.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setMessage(errorMessage);
            } else {
                builder.setMessage(R.string.download_error_error_unknown);
            }
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel());
            builder.setOnDismissListener(dialog -> {
                setResult(RESULT_ERROR);
                finish();
            });
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog = builder.show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void playbackStateChanged(PlayerStatusEvent event) {
        boolean isPlayingPreview =
                PlaybackPreferences.getCurrentlyPlayingMediaType() == RemoteMedia.PLAYABLE_TYPE_REMOTE_MEDIA;
        viewBinding.stopPreviewButton.setVisibility(isPlayingPreview ? View.VISIBLE : View.GONE);
    }

    /**
     *
     * @return true if a FeedDiscoveryDialog is shown, false otherwise (e.g., due to no feed found).
     */
    private boolean showFeedDiscoveryDialog(File feedFile, String baseUrl) {
        FeedDiscoverer fd = new FeedDiscoverer();
        final Map<String, String> urlsMap;
        try {
            urlsMap = fd.findLinks(feedFile, baseUrl);
            if (urlsMap == null || urlsMap.isEmpty()) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (isPaused || isFinishing()) {
            return false;
        }

        final List<String> titles = new ArrayList<>();

        final List<String> urls = new ArrayList<>(urlsMap.keySet());
        for (String url : urls) {
            titles.add(urlsMap.get(url));
        }

        if (urls.size() == 1) {
            // Skip dialog and display the item directly
            resetIntent(urls.get(0));
            feedDownloader.startFeedDownload(urls.get(0), null, null, (result) -> {checkDownloadResult(result);
                return null;
            });
            return true;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(OnlineFeedViewActivity.this, R.layout.ellipsize_start_listitem, R.id.txtvTitle, titles);
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            String selectedUrl = urls.get(which);
            dialog.dismiss();
            resetIntent(selectedUrl);
            FeedPreferences prefs = feed.getPreferences();
            if(prefs != null) {
                feedDownloader.startFeedDownload(selectedUrl, prefs.getUsername(), prefs.getPassword(), (status) -> {checkDownloadResult(status); return null;});
            } else {
                feedDownloader.startFeedDownload(selectedUrl, null, null, (status) -> {checkDownloadResult(status); return null;});
            }
        };

        AlertDialog.Builder ab = new AlertDialog.Builder(OnlineFeedViewActivity.this)
                .setTitle(R.string.feeds_label)
                .setCancelable(true)
                .setOnCancelListener(dialog -> finish())
                .setAdapter(adapter, onClickListener);

        runOnUiThread(() -> {
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog = ab.show();
        });
        return true;
    }

    private class FeedViewAuthenticationDialog extends AuthenticationDialog {

        private final String feedUrl;

        FeedViewAuthenticationDialog(Context context, int titleRes, String feedUrl) {
            super(context, titleRes, true, null, null);
            this.feedUrl = feedUrl;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            finish();
        }

        @Override
        protected void onConfirmed(String username, String password) {
            feedDownloader.startFeedDownload(feedUrl, username, password, (status) -> {checkDownloadResult(status); return null;});
        }
    }

}
