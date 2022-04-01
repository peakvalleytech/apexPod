package de.danoeh.apexpod.adapter.discovery;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.activity.OnlineFeedViewActivity;
import de.danoeh.apexpod.activity.discovery.FeedDownloader;
import de.danoeh.apexpod.core.dialog.DownloadRequestErrorDialogCreator;
import de.danoeh.apexpod.core.storage.DownloadRequestException;
import de.danoeh.apexpod.core.storage.DownloadRequester;
import de.danoeh.apexpod.discovery.PodcastSearchResult;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.parser.feed.FeedHandler;
import de.danoeh.apexpod.parser.feed.FeedHandlerResult;
import de.danoeh.apexpod.parser.feed.UnsupportedFeedtypeException;
import de.danoeh.apexpod.ui.common.ThemeUtils;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;

public class PodcastSearchResultAdapter extends
        RecyclerView.Adapter<PodcastSearchResultAdapter.PodcastRecyclerViewHolder> {
    private static final String TAG = "PodcastSearchResultAtr";
    private MainActivity activity;
    /**
     * Related Context
     */
    private final Context context;
    private Disposable parser;
    /**
     * List holding the podcasts found in the search
     */
    private final List<PodcastSearchResult> data;
    private List<Feed> subscribedFeeds;
    private Set<String> subscribedFeedAuthors;
    private Set<String> subscribedFeedTitle;
    private FeedDownloader feedDownloader;

    public PodcastSearchResultAdapter(MainActivity mainActivity, Context context, List<PodcastSearchResult> data, List<Feed> subscribedFeeds) {
        this.context = context;
        this.data = data;
        this.activity = mainActivity;
        this.feedDownloader = new FeedDownloader(mainActivity);
        this.subscribedFeeds = subscribedFeeds;
        this.subscribedFeedAuthors = new HashSet<>();
        if (subscribedFeeds != null) {
            for (Feed f : subscribedFeeds) {
                subscribedFeedAuthors.add(f.getAuthor());
            }

        }
    }

    @NonNull
    @Override
    public PodcastRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itunes_podcast_listitem, parent, false);
        return new PodcastRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastRecyclerViewHolder holder, int position) {
        PodcastSearchResult podcastSearchResult = data.get(position);
        holder.onBind(podcastSearchResult);
        holder.quickSubBtn.setOnClickListener(v -> {
            if (!isSubscribed(podcastSearchResult)) {
                feedDownloader.lookupUrl(
                        podcastSearchResult.feedUrl,
                        "",
                        "",
                        (result) -> {
                            if (result.isCancelled()) {
                                return null;
                            }
                            if (result.isSuccessful()) {
                                parser = Maybe.fromCallable(this::doParseFeed)
                                        .subscribeOn(Schedulers.computation())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeWith(new DisposableMaybeObserver<FeedHandlerResult>() {
                                            @Override
                                            public void onSuccess(@NonNull FeedHandlerResult result) {
                                                try {
                                                    DownloadRequester.getInstance().downloadFeed(v.getContext(), result.feed);
                                                    notifyItemChanged(holder.getBindingAdapterPosition());
                                                } catch (DownloadRequestException e) {
                                                    Log.e(TAG, Log.getStackTraceString(e));
                                                    DownloadRequestErrorDialogCreator.newRequestErrorDialog(v.getContext(), e.getMessage());
                                                }
                                            }

                                            @Override
                                            public void onComplete() {
                                                // Ignore null result: We showed the discovery dialog.
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable error) {
//                                            showErrorDialog(error.getMessage(), "");
                                                Log.d(TAG, "Feed parser exception: " + Log.getStackTraceString(error));
                                            }
                                        });
                            }
                            return null;
                        }, () -> {
                            return null;
                        }
                );
            } else {

            }
        });

    }

    private boolean isSubscribed(PodcastSearchResult podcastSearchResult) {
        return subscribedFeedAuthors.contains(podcastSearchResult.author);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateSubcribedList(List<Feed> subscribedFeeds) {
        this.subscribedFeeds = subscribedFeeds;
        if (subscribedFeeds != null) {
            for (Feed f : subscribedFeeds) {
                subscribedFeedAuthors.add(f.getAuthor());
            }

        }
        notifyDataSetChanged();
    }

    public class PodcastRecyclerViewHolder extends RecyclerView.ViewHolder {
        /**
         * ImageView holding the Podcast image
         */
        ImageView coverView;

        /**
         * TextView holding the Podcast title
         */
        TextView titleView;

        TextView authorView;

        View quickSubBtn;

        ImageView quickSubIcon;

        public PodcastRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            coverView = itemView.findViewById(R.id.imgvCover);
            titleView = itemView.findViewById(R.id.txtvTitle);
            authorView = itemView.findViewById(R.id.txtvAuthor);
            quickSubBtn = itemView.findViewById(R.id.quickSubBtn);
            quickSubIcon = itemView.findViewById(R.id.secondaryActionIcon);
        }

        public void onBind(@NonNull PodcastSearchResult podcastSearchResult) {
            if (isSubscribed(podcastSearchResult)) {
                quickSubIcon.setBackground(AppCompatResources.getDrawable(activity, R.drawable.ic_check));
            } else {
                quickSubIcon.setBackground(AppCompatResources.getDrawable(activity, R.drawable.ic_add));
            }
            titleView.setText(podcastSearchResult.title);
            if (podcastSearchResult.author != null && !podcastSearchResult.author.trim().isEmpty()) {
                authorView.setText(podcastSearchResult.author);
                authorView.setVisibility(View.VISIBLE);
            } else if (podcastSearchResult.feedUrl != null && !podcastSearchResult.feedUrl.contains("itunes.apple.com")) {
                authorView.setText(podcastSearchResult.feedUrl);
                authorView.setVisibility(View.VISIBLE);
            } else {
                authorView.setVisibility(View.GONE);
            }

            //Update the empty imageView with the image from the feed
            Glide.with(context)
                    .load(podcastSearchResult.imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.color.light_gray)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .transforms(new FitCenter(),
                                    new RoundedCorners((int) (4 * context.getResources().getDisplayMetrics().density)))
                            .dontAnimate())
                    .into(coverView);

            itemView.setOnClickListener(v -> {
                if (podcastSearchResult.feedUrl == null) {
                    return;
                }
                Intent intent = new Intent(context, OnlineFeedViewActivity.class);
                intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, podcastSearchResult.feedUrl);
                activity.startActivity(intent);
            });
            itemView.setBackgroundResource(ThemeUtils.getDrawableFromAttr(activity, R.attr.selectableItemBackground));

        }
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
        Feed feed = feedDownloader.getFeed();
        try {
            return handler.parseFeed(feed);
        } catch (UnsupportedFeedtypeException e) {
            Log.d(TAG, "Unsupported feed type detected");
//            if ("html".equalsIgnoreCase(e.getRootElement())) {
//                boolean dialogShown = showFeedDiscoveryDialog(new File(feed.getFile_url()), feed.getDownload_url());
//                if (dialogShown) {
                    return null; // Should not display an error message
//                } else {
//                    throw new UnsupportedFeedtypeException(getString(R.string.download_error_unsupported_type_html));
//                }
//            } else {
//                throw e;
//            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw e;
        } finally {
            boolean rc = new File(feed.getFile_url()).delete();
            Log.d(TAG, "Deleted feed source file. Result: " + rc);
        }
    }

    public void dispose() {
        if (parser != null)
        parser.dispose();
    }
}
