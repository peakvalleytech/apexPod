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
    public FeedTagAdapter(Context context, List<NavDrawerData.TagDrawerItem> feedFolders) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.defaultAll = new NavDrawerData.TagDrawerItem("All");//context.getString(R.string.tag_all));
        this.feedFolders = feedFolders;
        defaultAll.id = RecyclerView.NO_ID;
        init();
    }

    private void init() {
        if (this.feedFolders.size() == 0) {
            this.feedFolders.add(defaultAll);
        }
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

            if (tag.name.equals("All")) {
                clearTagFilterIds();
            } else {
                if (holder.chip.isChecked()) {
                    addTagFilterId(tag.id);
                } else {
                    removeTagFilterId(tag.id);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedFolders.size();
    }

    public boolean isEmpyty() {
        if (feedFolders.size() == 1 && feedFolders.get(0).id == defaultAll.id) {
            return true;

        } else {
            return false;
        }
    }

    public List<NavDrawerData.TagDrawerItem> getFeedFolders() {
        return feedFolders;
    }

    public void addItem(NavDrawerData.TagDrawerItem tagDrawerItem) {
        feedFolders.add(tagDrawerItem);
        notifyDataSetChanged();
    }

    public void clear() {
        feedFolders.clear();
        init();
        notifyDataSetChanged();
    }

    public class TagViewHolder extends RecyclerView.ViewHolder {
        private Chip chip;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.feedChip);
        }

        public void bind(NavDrawerData.TagDrawerItem tagDrawerItem) {
            chip.setText(tagDrawerItem.name);
        }
    }

    public void addTagFilterId(long tagFilterId) {
        Set<String> tagFilterIds = new HashSet<>(prefs.getStringSet(PREF_TAG_FILTER, new HashSet<>()));
        tagFilterIds.add(String.valueOf(tagFilterId));
        prefs.edit().putStringSet(PREF_TAG_FILTER, null).apply();
        prefs.edit().putStringSet(PREF_TAG_FILTER, tagFilterIds)
                .apply();
    }

    public void removeTagFilterId(long tagFilterId) {
        Set<String> tagFilterIds = new HashSet<>(prefs.getStringSet(PREF_TAG_FILTER, new HashSet<>()));
        tagFilterIds.remove(String.valueOf(tagFilterId));
        prefs.edit().putStringSet(PREF_TAG_FILTER, null).apply();
        prefs.edit().putStringSet(PREF_TAG_FILTER, tagFilterIds)
                .apply();
    }

    public void clearTagFilterIds() {
        prefs.edit().putStringSet(PREF_TAG_FILTER, null).apply();
        prefs.edit().putStringSet(PREF_TAG_FILTER, new HashSet<>()).apply();
        notifyDataSetChanged();
    }
}
