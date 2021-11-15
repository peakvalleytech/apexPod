package de.danoeh.apexpod.adapter.discovery;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.activity.OnlineFeedViewActivity;
import de.danoeh.apexpod.discovery.PodcastSearchResult;
import de.danoeh.apexpod.ui.common.ThemeUtils;

public class PodcastSearchResultAdapter extends
        RecyclerView.Adapter<PodcastSearchResultAdapter.PodcastRecyclerViewHolder> {
    private MainActivity activity;
    /**
     * Related Context
     */
    private final Context context;

    /**
     * List holding the podcasts found in the search
     */
    private final List<PodcastSearchResult> data;

    public PodcastSearchResultAdapter(MainActivity mainActivity, Context context, List<PodcastSearchResult> data) {
        this.context = context;
        this.data = data;
        this.activity = mainActivity;
    }

    @NonNull
    @Override
    public PodcastRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itunes_podcast_listitem, parent, false);
        return new PodcastRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastRecyclerViewHolder holder, int position) {
        holder.onBind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
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
            quickSubIcon.setBackground(AppCompatResources.getDrawable(activity, R.drawable.ic_add));
            titleView.setText(podcastSearchResult.title);
        if (podcastSearchResult.author != null && ! podcastSearchResult.author.trim().isEmpty()) {
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

        quickSubBtn.setOnClickListener(v -> {
            quickSubIcon.setBackground(AppCompatResources.getDrawable(activity,R.drawable.ic_check_circle_black_24dp));
        });
        }
    }
}
