package de.danoeh.apexpod.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.core.event.FavoritesEvent;
import de.danoeh.apexpod.core.event.PlaybackPositionEvent;
import de.danoeh.apexpod.core.event.ServiceEvent;
import de.danoeh.apexpod.model.feed.Chapter;
import de.danoeh.apexpod.core.event.UnreadItemsUpdateEvent;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedMedia;
import de.danoeh.apexpod.core.feed.util.PlaybackSpeedUtils;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.service.playback.PlaybackService;
import de.danoeh.apexpod.core.util.ChapterUtils;
import de.danoeh.apexpod.core.util.Converter;
import de.danoeh.apexpod.core.util.IntentUtils;
import de.danoeh.apexpod.core.util.TimeSpeedConverter;
import de.danoeh.apexpod.core.util.playback.MediaPlayerError;
import de.danoeh.apexpod.model.playback.Playable;
import de.danoeh.apexpod.core.util.playback.PlaybackController;
import de.danoeh.apexpod.dialog.SkipPreferenceDialog;
import de.danoeh.apexpod.dialog.SleepTimerDialog;
import de.danoeh.apexpod.dialog.VariableSpeedDialog;
import de.danoeh.apexpod.menuhandler.FeedItemMenuHandler;
import de.danoeh.apexpod.ui.common.PlaybackSpeedIndicatorView;
import de.danoeh.apexpod.view.ChapterSeekBar;
import de.danoeh.apexpod.view.PlayButton;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Shows the audio player.
 */
public class AudioPlayerFragment extends Fragment implements
        ChapterSeekBar.OnSeekBarChangeListener, Toolbar.OnMenuItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = "AudioPlayerFragment";
    public static final int POS_COVER = 0;
    public static final int POS_DESCRIPTION = 1;
    public static final int POS_LOOP_MODE = 2;
    private static final int NUM_CONTENT_FRAGMENTS = 3;
    private static final int AUDIO_CONTROL_ENABLED_ALPHA = 255;
    private static final int AUDIO_CONTROL_DISABLED_ALPHA = 64;



    PlaybackSpeedIndicatorView butPlaybackSpeed;
    TextView txtvPlaybackSpeed;
    private ViewPager2 pager;
    private TextView txtvPosition;
    private TextView txtvLength;
    private ChapterSeekBar sbPosition;
    private ImageButton butRev;
    private TextView txtvRev;
    private PlayButton butPlay;
    private ImageButton butFF;
    private TextView txtvFF;
    private ImageButton butSkip;
    private Toolbar toolbar;
    private ProgressBar progressIndicator;
    private CardView cardViewSeek;
    private TextView txtvSeek;
    private ImageView repeatAudioControlImgView;

    private PlaybackController controller;
    private Disposable disposable;
    private boolean showTimeLeft;
    private boolean seekedToChapterStart = false;
    private int currentChapterIndex = -1;
    private int duration;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.audioplayer_fragment, container, false);
        root.setOnTouchListener((v, event) -> true); // Avoid clicks going through player to fragments below
        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(v ->
                ((MainActivity) getActivity()).getBottomSheet().setState(BottomSheetBehavior.STATE_COLLAPSED));
        toolbar.setOnMenuItemClickListener(this);

        ExternalPlayerFragment externalPlayerFragment = new ExternalPlayerFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.playerFragment, externalPlayerFragment, ExternalPlayerFragment.TAG)
                .commit();

        butPlaybackSpeed = root.findViewById(R.id.butPlaybackSpeed);
        txtvPlaybackSpeed = root.findViewById(R.id.txtvPlaybackSpeed);
        sbPosition = root.findViewById(R.id.sbPosition);
        txtvPosition = root.findViewById(R.id.txtvPosition);
        txtvLength = root.findViewById(R.id.txtvLength);
        butRev = root.findViewById(R.id.butRev);
        txtvRev = root.findViewById(R.id.txtvRev);
        butPlay = root.findViewById(R.id.butPlay);
        butFF = root.findViewById(R.id.butFF);
        txtvFF = root.findViewById(R.id.txtvFF);
        butSkip = root.findViewById(R.id.butSkip);
        progressIndicator = root.findViewById(R.id.progLoading);
        cardViewSeek = root.findViewById(R.id.cardViewSeek);
        txtvSeek = root.findViewById(R.id.txtvSeek);
//        repeatAudioControlImgView = root.findViewById(R.id.repeat_episode);
//        repeatAudioControlImgView.setImageAlpha(UserPreferences.getShouldRepeatEpisode() ?
//                AUDIO_CONTROL_ENABLED_ALPHA : AUDIO_CONTROL_DISABLED_ALPHA);

        setupLengthTextView();
        setupControlButtons();
        butPlaybackSpeed.setOnClickListener(v -> new VariableSpeedDialog().show(getChildFragmentManager(), null));
        sbPosition.setOnSeekBarChangeListener(this);

        pager = root.findViewById(R.id.pager);
        pager.setAdapter(new AudioPlayerPagerAdapter(this, controller));
        // Required for getChildAt(int) in ViewPagerBottomSheetBehavior to return the correct page
        pager.setOffscreenPageLimit((int) NUM_CONTENT_FRAGMENTS);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                pager.post(() -> {
                    if (getActivity() != null) {
                        // By the time this is posted, the activity might be closed again.
                        ((MainActivity) getActivity()).getBottomSheet().updateScrollingChild();
                    }
                });
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);


        return root;
    }

    private void setChapterDividers(Playable media) {

        if (media == null) {
            return;
        }

        float[] dividerPos = null;

        if (media.getChapters() != null && !media.getChapters().isEmpty()) {
            List<Chapter> chapters = media.getChapters();
            dividerPos = new float[chapters.size()];

            for (int i = 0; i < chapters.size(); i++) {
                dividerPos[i] = chapters.get(i).getStart() / (float) duration;
            }
        }

        sbPosition.setDividerPos(dividerPos);
    }

    public View getExternalPlayerHolder() {
        return getView().findViewById(R.id.playerFragment);
    }

    private void setupControlButtons() {
        butRev.setOnClickListener(v -> {
            if (controller != null) {
                int curr = controller.getPosition();
                controller.seekTo(curr - UserPreferences.getRewindSecs() * 1000);
            }
        });
        butRev.setOnLongClickListener(v -> {
            SkipPreferenceDialog.showSkipPreference(getContext(),
                    SkipPreferenceDialog.SkipDirection.SKIP_REWIND, txtvRev);
            return true;
        });
        butPlay.setOnClickListener(v -> {
            if (controller != null) {
                controller.init();
                controller.playPause();
            }
        });
        butFF.setOnClickListener(v -> {
            if (controller != null) {
                int curr = controller.getPosition();
                controller.seekTo(curr + UserPreferences.getFastForwardSecs() * 1000);
            }
        });
        butFF.setOnLongClickListener(v -> {
            SkipPreferenceDialog.showSkipPreference(getContext(),
                    SkipPreferenceDialog.SkipDirection.SKIP_FORWARD, txtvFF);
            return false;
        });
        butSkip.setOnClickListener(v ->
                IntentUtils.sendLocalBroadcast(getActivity(), PlaybackService.ACTION_SKIP_CURRENT_EPISODE));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadItemsUpdate(UnreadItemsUpdateEvent event) {
        if (controller == null) {
            return;
        }
        updatePosition(new PlaybackPositionEvent(controller.getPosition(),
                controller.getDuration()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackServiceChanged(ServiceEvent event) {
        if (event.action == ServiceEvent.Action.SERVICE_SHUT_DOWN) {
            ((MainActivity) getActivity()).getBottomSheet().setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void setupLengthTextView() {
        showTimeLeft = UserPreferences.shouldShowRemainingTime();
        txtvLength.setOnClickListener(v -> {
            if (controller == null) {
                return;
            }
            showTimeLeft = !showTimeLeft;
            UserPreferences.setShowRemainTimeSetting(showTimeLeft);
            updatePosition(new PlaybackPositionEvent(controller.getPosition(),
                    controller.getDuration()));
        });
    }

    protected void updatePlaybackSpeedButton(Playable media) {
        if (butPlaybackSpeed == null || controller == null) {
            return;
        }
        float speed = PlaybackSpeedUtils.getCurrentPlaybackSpeed(media);
        String speedStr = new DecimalFormat("0.00").format(speed);
        txtvPlaybackSpeed.setText(speedStr);
        butPlaybackSpeed.setSpeed(speed);
    }

    private void loadMediaInfo(boolean includingChapters) {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Maybe.<Playable>create(emitter -> {
            Playable media = controller.getMedia();
            if (media != null) {
                if (includingChapters) {
                    ChapterUtils.loadChapters(media, getContext());
                }
                emitter.onSuccess(media);
            } else {
                emitter.onComplete();
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(media -> {
            updateUi(media);
            if (media.getChapters() == null && !includingChapters) {
                loadMediaInfo(true);
            }
        }, error -> Log.e(TAG, Log.getStackTraceString(error)),
            () -> updateUi(null));
    }

    private PlaybackController newPlaybackController() {
        return new PlaybackController(getActivity()) {
            @Override
            public void onBufferStart() {
                progressIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBufferEnd() {
                progressIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onBufferUpdate(float progress) {
                if (isStreaming()) {
                    sbPosition.setSecondaryProgress((int) (progress * sbPosition.getMax()));
                } else {
                    sbPosition.setSecondaryProgress(0);
                }
            }

            @Override
            public void handleError(int code) {
                final AlertDialog.Builder errorDialog = new AlertDialog.Builder(getContext());
                errorDialog.setTitle(R.string.error_label);
                errorDialog.setMessage(MediaPlayerError.getErrorString(getContext(), code));
                errorDialog.setPositiveButton(android.R.string.ok, (dialog, which) ->
                        ((MainActivity) getActivity()).getBottomSheet().setState(BottomSheetBehavior.STATE_COLLAPSED));
                if (!UserPreferences.useExoplayer()) {
                    errorDialog.setNeutralButton(R.string.media_player_switch_to_exoplayer, (dialog, which) -> {
                        UserPreferences.enableExoplayer();
                        ((MainActivity) getActivity()).showSnackbarAbovePlayer(
                                R.string.media_player_switched_to_exoplayer, Snackbar.LENGTH_LONG);
                    });
                }
                errorDialog.create().show();
            }

            @Override
            public void onSleepTimerUpdate() {
                AudioPlayerFragment.this.loadMediaInfo(false);
            }

            @Override
            protected void updatePlayButtonShowsPlay(boolean showPlay) {
                butPlay.setIsShowPlay(showPlay);
            }

            @Override
            public void loadMediaInfo() {
                AudioPlayerFragment.this.loadMediaInfo(false);
            }

            @Override
            public void onPlaybackEnd() {
                ((MainActivity) getActivity()).getBottomSheet().setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            @Override
            public void onPlaybackSpeedChange() {
                updatePlaybackSpeedButton(getMedia());
            }
        };
    }

    private void updateUi(Playable media) {
        if (controller == null) {
            return;
        }
        duration = controller.getDuration();
        updatePosition(new PlaybackPositionEvent(controller.getPosition(), duration));
        updatePlaybackSpeedButton(media);
        setChapterDividers(media);
        setupOptionsMenu(media);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        controller = newPlaybackController();

        controller.init();
        loadMediaInfo(false);
        EventBus.getDefault().register(this);
        txtvRev.setText(NumberFormat.getInstance().format(UserPreferences.getRewindSecs()));
        txtvFF.setText(NumberFormat.getInstance().format(UserPreferences.getFastForwardSecs()));
    }

    @Override
    public void onStop() {
        super.onStop();
        controller.release();
        controller = null;
        progressIndicator.setVisibility(View.GONE); // Controller released; we will not receive buffering updates
        EventBus.getDefault().unregister(this);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updatePosition(PlaybackPositionEvent event) {
        if (controller == null || txtvPosition == null || txtvLength == null || sbPosition == null) {
            return;
        }

        TimeSpeedConverter converter = new TimeSpeedConverter(controller.getCurrentPlaybackSpeedMultiplier());
        int currentPosition = converter.convert(event.getPosition());
        int duration = converter.convert(event.getDuration());
        int remainingTime = converter.convert(Math.max(event.getDuration() - event.getPosition(), 0));
        currentChapterIndex = ChapterUtils.getCurrentChapterIndex(controller.getMedia(), currentPosition);
        Log.d(TAG, "currentPosition " + Converter.getDurationStringLong(currentPosition));
        if (currentPosition == PlaybackService.INVALID_TIME || duration == PlaybackService.INVALID_TIME) {
            Log.w(TAG, "Could not react to position observer update because of invalid time");
            return;
        }
        txtvPosition.setText(Converter.getDurationStringLong(currentPosition));
        showTimeLeft = UserPreferences.shouldShowRemainingTime();
        if (showTimeLeft) {
            txtvLength.setText(((remainingTime > 0) ? "-" : "") + Converter.getDurationStringLong(remainingTime));
        } else {
            txtvLength.setText(Converter.getDurationStringLong(duration));
        }

        if (!sbPosition.isPressed()) {
            float progress = ((float) event.getPosition()) / event.getDuration();
            sbPosition.setProgress((int) (progress * sbPosition.getMax()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void favoritesChanged(FavoritesEvent event) {
        AudioPlayerFragment.this.loadMediaInfo(false);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (controller == null || txtvLength == null) {
            return;
        }

        if (fromUser) {
            float prog = progress / ((float) seekBar.getMax());
            TimeSpeedConverter converter = new TimeSpeedConverter(controller.getCurrentPlaybackSpeedMultiplier());
            int position = converter.convert((int) (prog * controller.getDuration()));
            int newChapterIndex = ChapterUtils.getCurrentChapterIndex(controller.getMedia(), position);
            if (newChapterIndex > -1) {
                if (!sbPosition.isPressed() && currentChapterIndex != newChapterIndex) {
                    currentChapterIndex = newChapterIndex;
                    position = (int) controller.getMedia().getChapters().get(currentChapterIndex).getStart();
                    seekedToChapterStart = true;
                    controller.seekTo(position);
                    updateUi(controller.getMedia());
                    sbPosition.highlightCurrentChapter();
                }
                txtvSeek.setText(controller.getMedia().getChapters().get(newChapterIndex).getTitle()
                                + "\n" + Converter.getDurationStringLong(position));
            } else {
                txtvSeek.setText(Converter.getDurationStringLong(position));
            }
        } else if (duration != controller.getDuration()) {
            updateUi(controller.getMedia());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // interrupt position Observer, restart later
        cardViewSeek.setScaleX(.8f);
        cardViewSeek.setScaleY(.8f);
        cardViewSeek.animate()
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(200)
                .start();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (controller != null) {
            if (seekedToChapterStart) {
                seekedToChapterStart = false;
            } else {
                float prog = seekBar.getProgress() / ((float) seekBar.getMax());
                controller.seekTo((int) (prog * controller.getDuration()));
            }
        }
        cardViewSeek.setScaleX(1f);
        cardViewSeek.setScaleY(1f);
        cardViewSeek.animate()
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(0f).scaleX(.8f).scaleY(.8f)
                .setDuration(200)
                .start();
    }

    public void setupOptionsMenu(Playable media) {
        if (toolbar.getMenu().size() == 0) {
            toolbar.inflateMenu(R.menu.mediaplayer);
        }
        if (controller == null) {
            return;
        }
        boolean isFeedMedia = media instanceof FeedMedia;
        toolbar.getMenu().findItem(R.id.open_feed_item).setVisible(isFeedMedia);
        if (isFeedMedia) {
            FeedItemMenuHandler.onPrepareMenu(toolbar.getMenu(), ((FeedMedia) media).getItem());
            FeedItemMenuHandler.setItemVisibility(toolbar.getMenu(), R.id.add_to_playlist, true);
        }

        toolbar.getMenu().findItem(R.id.loop_mode).setOnMenuItemClickListener(item -> {
            pager.setCurrentItem(POS_LOOP_MODE);
            return true;
        });
        toolbar.getMenu().findItem(R.id.set_sleeptimer_item).setVisible(!controller.sleepTimerActive());
        toolbar.getMenu().findItem(R.id.disable_sleeptimer_item).setVisible(controller.sleepTimerActive());

//        ((CastEnabledActivity) getActivity()).requestCastButton(toolbar.getMenu());
        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_repeat, getContext().getTheme());
        icon.setAlpha(UserPreferences.getShouldRepeatEpisode() ?
                AUDIO_CONTROL_ENABLED_ALPHA :
                AUDIO_CONTROL_DISABLED_ALPHA);
        toolbar.getMenu().findItem(R.id.loop_mode).setIcon(icon);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (controller == null) {
            return false;
        }
        Playable media = controller.getMedia();
        if (media == null) {
            return false;
        }

        final @Nullable FeedItem feedItem = (media instanceof FeedMedia) ? ((FeedMedia) media).getItem() : null;
        if (feedItem != null && FeedItemMenuHandler.onMenuItemClicked(this, item.getItemId(), feedItem)) {
            return true;
        }
        int itemId = item.getItemId();
        if (itemId == R.id.disable_sleeptimer_item) {
            // Fall-through)
        } else if (itemId == R.id.set_sleeptimer_item) {
            new SleepTimerDialog().show(getChildFragmentManager(), "SleepTimerDialog");
            return true;
        } else if (itemId == R.id.open_feed_item) {
            if (feedItem != null) {
                Intent intent = MainActivity.getIntentToOpenFeed(getContext(), feedItem.getFeedId());
                startActivity(intent);
            }
            return true;
        }

        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (UserPreferences.PREF_REPEAT_EPISODE.equals(key)) {
            if (UserPreferences.getShouldRepeatEpisode()) {
//                repeatAudioControlImgView.setImageAlpha(AUDIO_CONTROL_ENABLED_ALPHA);
            } else {
//                repeatAudioControlImgView.setImageAlpha(AUDIO_CONTROL_DISABLED_ALPHA);
            }
            Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_repeat, getContext().getTheme());
            icon.setAlpha(UserPreferences.getShouldRepeatEpisode() ?
                    AUDIO_CONTROL_ENABLED_ALPHA :
                    AUDIO_CONTROL_DISABLED_ALPHA);
            toolbar.getMenu().findItem(R.id.loop_mode).setIcon(icon);
        }
    }

    private static class AudioPlayerPagerAdapter extends FragmentStateAdapter {
        private static final String TAG = "AudioPlayerPagerAdapter";
        private PlaybackController playbackController;
        public AudioPlayerPagerAdapter(@NonNull Fragment fragment, PlaybackController playbackController) {
            super(fragment);
            this.playbackController = playbackController;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Log.d(TAG, "getItem(" + position + ")");

            switch (position) {
                case POS_COVER:
                    return new CoverFragment();
                case POS_LOOP_MODE:
                    return new LoopModeFragment(playbackController);
                default:
                case POS_DESCRIPTION:
                    return new ItemDescriptionFragment();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_CONTENT_FRAGMENTS;
        }
    }

    public void scrollToPage(int page, boolean smoothScroll) {
        if (pager == null) {
            return;
        }

        pager.setCurrentItem(page, smoothScroll);

        Fragment visibleChild = getChildFragmentManager().findFragmentByTag("f" + POS_DESCRIPTION);
        if (visibleChild instanceof ItemDescriptionFragment) {
            ((ItemDescriptionFragment) visibleChild).scrollToTop();
        }
    }

    public void scrollToPage(int page) {
        scrollToPage(page, false);
    }
}
