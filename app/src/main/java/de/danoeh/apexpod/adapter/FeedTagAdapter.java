package de.danoeh.apexpod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.storage.NavDrawerData;

public class FeedTagAdapter extends RecyclerView.Adapter<FeedTagAdapter.TagViewHolder> {
    private List<NavDrawerData.TagDrawerItem> feedFolders;
    private NavDrawerData.TagDrawerItem defaultAll;

    public FeedTagAdapter(Context context, List<NavDrawerData.TagDrawerItem> feedFolders) {
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
        if (feedFolders.size() > 1) {
            feedFolders.remove(defaultAll);
        }
        notifyDataSetChanged();
    }

    public void removeItem(NavDrawerData.TagDrawerItem tagDrawerItem) {
        this.feedFolders.remove(tagDrawerItem);
        if (feedFolders.size() == 0) {
            feedFolders.add(defaultAll);
        }
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
}
