package de.danoeh.apexpod.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.TextUtilsCompat;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.adapter.actionbutton.CancelDownloadActionButton;
import de.danoeh.apexpod.adapter.actionbutton.DeleteActionButton;
import de.danoeh.apexpod.adapter.actionbutton.DownloadActionButton;
import de.danoeh.apexpod.adapter.actionbutton.ItemActionButton;
import de.danoeh.apexpod.adapter.actionbutton.MarkAsPlayedActionButton;
import de.danoeh.apexpod.adapter.actionbutton.PauseActionButton;
import de.danoeh.apexpod.adapter.actionbutton.PlayActionButton;
import de.danoeh.apexpod.adapter.actionbutton.PlayLocalActionButton;
import de.danoeh.apexpod.adapter.actionbutton.StreamActionButton;
import de.danoeh.apexpod.adapter.actionbutton.VisitWebsiteActionButton;
import de.danoeh.apexpod.core.event.DownloadEvent;
import de.danoeh.apexpod.core.event.DownloaderUpdate;
import de.danoeh.apexpod.core.event.FeedItemEvent;
import de.danoeh.apexpod.core.event.PlayerStatusEvent;
import de.danoeh.apexpod.core.event.UnreadItemsUpdateEvent;
import de.danoeh.apexpod.core.preferences.PlaybackPreferences;
import de.danoeh.apexpod.fragment.feed.FeedItemlistFragment;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.core.feed.util.ImageResourceUtils;
import de.danoeh.apexpod.core.glide.ApGlideSettings;
import de.danoeh.apexpod.core.preferences.UsageStatistics;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.service.download.Downloader;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DownloadRequester;
import de.danoeh.apexpod.core.util.Converter;
import de.danoeh.apexpod.core.util.DateFormatter;
import de.danoeh.apexpod.core.util.FeedItemUtil;
import de.danoeh.apexpod.ui.common.ThemeUtils;
import de.danoeh.apexpod.core.util.playback.PlaybackController;
import de.danoeh.apexpod.core.util.playback.Timeline;
import de.danoeh.apexpod.view.ShownotesWebView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.ArrayUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

/**
 * Displays information about a FeedItem and actions.
 */
public class ItemFragment extends Fragment {
    public static final String TAG = "ItemFragment";
    private static final String ARG_FEEDITEM = "feeditem";
    private static final String AUTOPLAY_MODE = "autoplayMode";
    private static final String AUTOPLAY_PLAYLIST_ID = "autoPlayPlaylistId";
    /**
     * Creates a new instance of an ItemFragment
     *
     * @param feeditem The ID of the FeedItem to show
     * @return The ItemFragment instance
     */
    public static ItemFragment newInstance(long feeditem,
                                           long autoPlayMode,
                                           long autoPlayPlaylistId) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_FEEDITEM, feeditem);
        args.putLong(AUTOPLAY_MODE, autoPlayMode);
        args.putLong(AUTOPLAY_PLAYLIST_ID, autoPlayPlaylistId);
        fragment.setArguments(args);
        return fragment;
    }

    private boolean itemsLoaded = false;
    private long itemId;
    private FeedItem item;
    private String webviewData;
    private List<Downloader> downloaderList;

    private ViewGroup root;
    private ShownotesWebView webvDescription;
    private TextView txtvPodcast;
    private TextView txtvTitle;
    private TextView txtvDuration;
    private TextView txtvPublished;
    private ImageView imgvCover;
    private ProgressBar progbarDownload;
    private ProgressBar progbarLoading;
    private TextView butAction1Text;
    private TextView butAction2Text;
    private ImageView butAction1Icon;
    private ImageView butAction2Icon;
    private View butAction1;
    private View butAction2;
    private ItemActionButton actionButton1;
    private ItemActionButton actionButton2;
    private View noMediaLabel;

    private Disposable disposable;
    private PlaybackController controller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemId = getArguments().getLong(ARG_FEEDITEM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.feeditem_fragment, container, false);

        root = layout.findViewById(R.id.content_root);

        txtvPodcast = layout.findViewById(R.id.txtvPodcast);
        txtvPodcast.setOnClickListener(v -> openPodcast());
        txtvTitle = layout.findViewById(R.id.txtvTitle);
        if (Build.VERSION.SDK_INT >= 23) {
            txtvTitle.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);
        }
        txtvDuration = layout.findViewById(R.id.txtvDuration);
        txtvPublished = layout.findViewById(R.id.txtvPublished);
        txtvTitle.setEllipsize(TextUtils.TruncateAt.END);
        webvDescription = layout.findViewById(R.id.webvDescription);
        webvDescription.setTimecodeSelectedListener(time -> {
            if (controller != null && item.getMedia() != null && controller.getMedia() != null
                    && ObjectsCompat.equals(item.getMedia().getIdentifier(), controller.getMedia().getIdentifier())) {
                controller.seekTo(time);
            } else {
                ((MainActivity) getActivity()).showSnackbarAbovePlayer(R.string.play_this_to_seek_position,
                        Snackbar.LENGTH_LONG);
            }
        });
        registerForContextMenu(webvDescription);

        imgvCover = layout.findViewById(R.id.imgvCover);
        imgvCover.setOnClickListener(v -> openPodcast());
        progbarDownload = layout.findViewById(R.id.progbarDownload);
        progbarLoading = layout.findViewById(R.id.progbarLoading);
        butAction1 = layout.findViewById(R.id.butAction1);
        butAction2 = layout.findViewById(R.id.butAction2);
        butAction1Icon = layout.findViewById(R.id.butAction1Icon);
        butAction2Icon = layout.findViewById(R.id.butAction2Icon);
        butAction1Text = layout.findViewById(R.id.butAction1Text);
        butAction2Text = layout.findViewById(R.id.butAction2Text);
        noMediaLabel = layout.findViewById(R.id.noMediaLabel);

        butAction1.setOnClickListener(v -> {
            if (actionButton1 instanceof StreamActionButton && !UserPreferences.isStreamOverDownload()
                    && UsageStatistics.hasSignificantBiasTo(UsageStatistics.ACTION_STREAM)) {
                showOnDemandConfigBalloon(true);
                return;
            }
            actionButton1.onClick(getContext());
        });
        butAction2.setOnClickListener(v -> {
            if (actionButton2 instanceof DownloadActionButton && UserPreferences.isStreamOverDownload()
                    && UsageStatistics.hasSignificantBiasTo(UsageStatistics.ACTION_DOWNLOAD)) {
                showOnDemandConfigBalloon(false);
                return;
            }
            actionButton2.onClick(getContext());
        });
        return layout;
    }

    private void showOnDemandConfigBalloon(boolean offerStreaming) {
        boolean isLocaleRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
                == ViewCompat.LAYOUT_DIRECTION_RTL;
        Balloon balloon = new Balloon.Builder(getContext())
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPosition(0.25f + ((isLocaleRtl ^ offerStreaming) ? 0f : 0.5f))
                .setWidthRatio(1.0f)
                .isRtlSupport(true)
                .setBackgroundColor(ThemeUtils.getColorFromAttr(getContext(), R.attr.colorSecondary))
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setLayout(R.layout.popup_bubble_view)
                .setDismissWhenTouchOutside(true)
                .setLifecycleOwner(this)
                .build();
        Button positiveButton = balloon.getContentView().findViewById(R.id.balloon_button_positive);
        Button negativeButton = balloon.getContentView().findViewById(R.id.balloon_button_negative);
        TextView message = balloon.getContentView().findViewById(R.id.balloon_message);
        message.setText(offerStreaming
                ? R.string.on_demand_config_stream_text : R.string.on_demand_config_download_text);
        positiveButton.setOnClickListener(v1 -> {
            UserPreferences.setStreamOverDownload(offerStreaming);
            // Update all visible lists to reflect new streaming action button
            EventBus.getDefault().post(new UnreadItemsUpdateEvent());
            ((MainActivity) getActivity()).showSnackbarAbovePlayer(
                    R.string.on_demand_config_setting_changed, Snackbar.LENGTH_SHORT);
            balloon.dismiss();
        });
        negativeButton.setOnClickListener(v1 -> {
            UsageStatistics.askAgainLater(UsageStatistics.ACTION_STREAM); // Type does not matter. Both are silenced.
            balloon.dismiss();
        });
        balloon.showAlignBottom(butAction1, 0, (int) (-12 * getResources().getDisplayMetrics().density));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        load();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        controller = new PlaybackController(getActivity()) {
            @Override
            public void loadMediaInfo() {
                // Do nothing
            }
        };
        controller.init();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (itemsLoaded) {
            progbarLoading.setVisibility(View.GONE);
            updateAppearance();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        controller.release();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null) {
            disposable.dispose();
        }
        if (webvDescription != null && root != null) {
            root.removeView(webvDescription);
            webvDescription.destroy();
        }
    }

    private void onFragmentLoaded() {
        if (webviewData != null) {
            webvDescription.loadDataWithBaseURL("https://127.0.0.1", webviewData, "text/html", "utf-8", "about:blank");
        }
        updateAppearance();
    }

    private void updateAppearance() {
        if (item == null) {
            Log.d(TAG, "updateAppearance item is null");
            return;
        }
        txtvPodcast.setText(item.getFeed().getTitle());
        txtvTitle.setText(item.getTitle());

        if (item.getPubDate() != null) {
            String pubDateStr = DateFormatter.formatAbbrev(getActivity(), item.getPubDate());
            txtvPublished.setText(pubDateStr);
            txtvPublished.setContentDescription(DateFormatter.formatForAccessibility(getContext(), item.getPubDate()));
        }

        RequestOptions options = new RequestOptions()
                .error(R.color.light_gray)
                .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                .transforms(new FitCenter(),
                        new RoundedCorners((int) (4 * getResources().getDisplayMetrics().density)))
                .dontAnimate();

        Glide.with(getActivity())
                .load(item.getImageLocation())
                .error(Glide.with(getActivity())
                        .load(ImageResourceUtils.getFallbackImageLocation(item))
                        .apply(options))
                .apply(options)
                .into(imgvCover);
        updateButtons();
    }

    private void updateButtons() {
        progbarDownload.setVisibility(View.GONE);
        if (item.hasMedia() && downloaderList != null) {
            for (Downloader downloader : downloaderList) {
                if (downloader.getDownloadRequest().getFeedfileType() == FeedMedia.FEEDFILETYPE_FEEDMEDIA
                        && downloader.getDownloadRequest().getFeedfileId() == item.getMedia().getId()) {
                    progbarDownload.setVisibility(View.VISIBLE);
                    progbarDownload.setProgress(downloader.getDownloadRequest().getProgressPercent());
                }
            }
        }

        FeedMedia media = item.getMedia();
        if (media == null) {
            actionButton1 = new MarkAsPlayedActionButton(item);
            actionButton2 = new VisitWebsiteActionButton(item);
            noMediaLabel.setVisibility(View.VISIBLE);
        } else {
            noMediaLabel.setVisibility(View.GONE);
            if (media.getDuration() > 0) {
                txtvDuration.setText(Converter.getDurationStringLong(media.getDuration()));
                txtvDuration.setContentDescription(
                        Converter.getDurationStringLocalized(getContext(), media.getDuration()));
            }
            Bundle args = getArguments();
            long autoPlayMode = args.getLong(AUTOPLAY_MODE);
            long autoPlayPlaylistId = 0;

            if (autoPlayMode == PlaybackPreferences.AUTOPLAY_FEED) {
                autoPlayPlaylistId = item.getFeedId();
            } else {
                autoPlayPlaylistId = args.getLong(AUTOPLAY_PLAYLIST_ID);
            }

            if (FeedItemUtil.isCurrentlyPlaying(media)) {
                actionButton1 = new PauseActionButton(item);
            } else if (item.getFeed().isLocalFeed()) {
                actionButton1 = new PlayLocalActionButton(item);
            } else if (media.isDownloaded()) {
                actionButton1 = new PlayActionButton(item, autoPlayMode, autoPlayPlaylistId);
            } else {
                actionButton1 = new StreamActionButton(item, autoPlayMode, autoPlayPlaylistId);
            }
            if (DownloadRequester.getInstance().isDownloadingFile(media)) {
                actionButton2 = new CancelDownloadActionButton(item);
            } else if (!media.isDownloaded()) {
                actionButton2 = new DownloadActionButton(item);
            } else {
                actionButton2 = new DeleteActionButton(item);
            }
        }

        butAction1Text.setText(actionButton1.getLabel());
        butAction1Text.setTransformationMethod(null);
        butAction1Icon.setImageResource(actionButton1.getDrawable());
        butAction1.setVisibility(actionButton1.getVisibility());

        butAction2Text.setText(actionButton2.getLabel());
        butAction2Text.setTransformationMethod(null);
        butAction2Icon.setImageResource(actionButton2.getDrawable());
        butAction2.setVisibility(actionButton2.getVisibility());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return webvDescription.onContextItemSelected(item);
    }

    private void openPodcast() {
        Fragment fragment = FeedItemlistFragment.newInstance(item.getFeedId());
        ((MainActivity) getActivity()).loadChildFragment(fragment);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FeedItemEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        for (FeedItem item : event.items) {
            if (this.item.getId() == item.getId()) {
                load();
                return;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        DownloaderUpdate update = event.update;
        downloaderList = update.downloaders;
        if (item == null || item.getMedia() == null) {
            return;
        }
        long mediaId = item.getMedia().getId();
        if (ArrayUtils.contains(update.mediaIds, mediaId)) {
            if (itemsLoaded && getActivity() != null) {
                updateAppearance();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusChanged(PlayerStatusEvent event) {
        updateButtons();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadItemsChanged(UnreadItemsUpdateEvent event) {
        load();
    }

    private void load() {
        if (disposable != null) {
            disposable.dispose();
        }
        if (!itemsLoaded) {
            progbarLoading.setVisibility(View.VISIBLE);
        }
        disposable = Observable.fromCallable(this::loadInBackground)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(result -> {
                progbarLoading.setVisibility(View.GONE);
                item = result;
                itemsLoaded = true;
                onFragmentLoaded();
            }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    @Nullable
    private FeedItem loadInBackground() {
        FeedItem feedItem = DBReader.getFeedItem(itemId);
        Context context = getContext();
        if (feedItem != null && context != null) {
            int duration = feedItem.getMedia() != null ? feedItem.getMedia().getDuration() : Integer.MAX_VALUE;
            DBReader.loadDescriptionOfFeedItem(feedItem);
            Timeline t = new Timeline(context, feedItem.getDescription(), duration);
            webviewData = t.processShownotes();
        }
        return feedItem;
    }

}
