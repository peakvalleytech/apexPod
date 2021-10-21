package de.danoeh.apexpod.fragment.swipeactions;

import android.content.Context;

import androidx.fragment.app.Fragment;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedItemFilter;

public class MarkFavoriteSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return MARK_FAV;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_star;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_yellow;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.add_to_favorite_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        DBWriter.toggleFavoriteItem(item);
    }

    @Override
    public boolean willRemove(FeedItemFilter filter) {
        return filter.showIsFavorite || filter.showNotFavorite;
    }
}
