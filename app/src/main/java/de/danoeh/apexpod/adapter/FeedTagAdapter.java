package de.danoeh.apexpod.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.storage.NavDrawerData;
import de.danoeh.apexpod.model.feed.FeedPreferences;

public class FeedTagAdapter extends RecyclerView.Adapter<FeedTagAdapter.TagViewHolder> {
    private List<NavDrawerData.TagDrawerItem> feedFolders;
    private NavDrawerData.TagDrawerItem defaultAll;
    private SharedPreferences prefs;
    public static final String PREF_TAG_FILTER = "prefTagFilter";
    private static final String PREFS = "SubscriptionFragment";
    public static final Long ID_ALL = -1L;
    public FeedTagAdapter(Context context, List<NavDrawerData.TagDrawerItem> feedFolders) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.defaultAll = new NavDrawerData.TagDrawerItem("All");//context.getString(R.string.tag_all));
        this.feedFolders = feedFolders;
        defaultAll.id = ID_ALL;
        this.feedFolders.add(defaultAll);

    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.feed_tag, parent, false);

        return new TagViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        holder.bind(feedFolders.get(position));

        holder.chip.setOnClickListener(v -> {
            NavDrawerData.TagDrawerItem tag = feedFolders.get(position);
            boolean isChecked = holder.chip.isChecked();

            if (isChecked) {
                setTagFilterId(tag.id);
            } else {
                setTagFilterId(defaultAll.id);
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return feedFolders.size();
    }

    public List<NavDrawerData.TagDrawerItem> getFeedFolders() {
        return feedFolders;
    }

    public void addItem(NavDrawerData.TagDrawerItem tagDrawerItem) {
        feedFolders.add(tagDrawerItem);
    }

    public class TagViewHolder extends RecyclerView.ViewHolder {
        private Chip chip;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.feedChip);
        }

        public void bind(NavDrawerData.TagDrawerItem tagDrawerItem) {
            chip.setText(tagDrawerItem.name);
            if (tagDrawerItem.id == getTagFilterId()) {
                chip.setChecked(true);
            } else {
                chip.setChecked(false);
            }
        }
    }

    public void setTagFilterId(long tagFilterId) {
        prefs.edit().putLong(PREF_TAG_FILTER, tagFilterId).apply();
    }

    public Long getTagFilterId() {
        return prefs.getLong(PREF_TAG_FILTER, defaultAll.id);
    }

}
