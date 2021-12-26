package de.danoeh.apexpod.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.core.glide.ApGlideSettings;
import de.danoeh.apexpod.core.preferences.PlaybackPreferences;
import de.danoeh.apexpod.core.storage.repository.HomeRepository;
import de.danoeh.apexpod.core.storage.repository.impl.HomeRepositoryImpl;
import de.danoeh.apexpod.core.util.FeedItemUtil;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.view.EmptyViewHandler;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class HomeFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
    public static final String TAG = "HomeFragment";
    private static final int NUM_SUGGESTIONS = 12;
    private static final String KEY_UP_ARROW = "up_arrow";
    private HomeRepository homeRepository;
    private FeedItem featuredFeedItem;

    private ImageView imgvCover;
    private TextView podcastTitle;
    private TextView episodeTitle;
    private Disposable disposable;

    private LinearLayout emptyViewLayout;
    private ConstraintLayout homeLayout;

    private Toolbar toolbar;
    private boolean displayUpArrow;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        homeRepository = new HomeRepositoryImpl();
        View root = inflater.inflate(R.layout.home_fragment, container, false);
        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this);
        displayUpArrow = getParentFragmentManager().getBackStackEntryCount() != 0;
        if (savedInstanceState != null) {
            displayUpArrow = savedInstanceState.getBoolean(KEY_UP_ARROW);
        }
        ((MainActivity) getActivity()).setupToolbarToggle(toolbar, displayUpArrow);
        toolbar.inflateMenu(R.menu.queue);

        imgvCover = root.findViewById(R.id.imgvCover);
        podcastTitle = root.findViewById(R.id.txtvPodcastTitle);
        episodeTitle = root.findViewById(R.id.txtvEpisodeTitle);
        emptyViewLayout = root.findViewById(R.id.empty_layout);

        imgvCover.setOnClickListener(v -> {
            long autoplayMode = PlaybackPreferences.AUTOPLAY_QUEUE;
            Fragment fragment = ItemFragment.newInstance(featuredFeedItem.getId(), autoplayMode, 0);
            MainActivity activity = (MainActivity) getActivity();
            long[] ids = new long[]{featuredFeedItem.getId()};
            activity.loadChildFragment(ItemPagerFragment.newInstance(ids,
                    0,
                    PlaybackPreferences.AUTOPLAY_QUEUE,
                    0
            ));
        });


        setupEmptyView(root);

        loadData();
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void loadData() {
        disposable = Maybe.fromCallable(homeRepository::getFeaturedEpisode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(item -> {
                   featuredFeedItem = item;
//                    homeLayout.s
                    emptyViewLayout.setVisibility(View.GONE);
                    Log.d(TAG, "Looded feeditem with name " + featuredFeedItem);
                    podcastTitle.setText(featuredFeedItem.getFeed().getTitle());
                    episodeTitle.setText(featuredFeedItem.getTitle());
                    RequestOptions options = new RequestOptions()
                            .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                            .dontAnimate()
                            .transforms(new FitCenter(),
                                    new RoundedCorners((int) (16 * getResources().getDisplayMetrics().density)));
                    RequestBuilder<Drawable> cover = Glide.with(this)
                            .load(featuredFeedItem.getImageLocation())
                            .error(Glide.with(this)
                                    .load(featuredFeedItem.getFeed().getImageUrl())
                                    .apply(options)
                            )
                            .apply(options);

                    cover.into(imgvCover);

                }, error -> Log.e(TAG, Log.getStackTraceString(error)));

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    private void setupEmptyView(View root) {
        ImageView emptyViewIcon = root.findViewById(R.id.emptyViewIcon);
        emptyViewIcon.setBackground(
                AppCompatResources.getDrawable(getActivity(), R.drawable.ic_folder)
        );
        emptyViewIcon.setVisibility(View.VISIBLE);
        TextView emptyViewTitle = root.findViewById(R.id.emptyViewTitle);
        emptyViewTitle.setText(R.string.no_subscriptions_head_label);
        TextView emptyViewMessage = root.findViewById(R.id.emptyViewMessage);
        emptyViewMessage.setText(R.string.no_subscriptions_label);
        emptyViewMessage.setText(R.string.no_subscriptions_label);
    }
}
