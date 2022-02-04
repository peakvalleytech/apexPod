package de.danoeh.apexpod.dialog.queue;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.danoeh.apexpod.core.event.QueueEvent;
import de.danoeh.apexpod.core.preferences.QueuePreferences;
import de.danoeh.apexpod.dialog.ChecklistDialog;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedItem;

public class QueueFeedFilterDialog {
    private static final String TAG = "QueueFeedFilterDia";
    private final Set<Long> filteredFeedIds;
    private final List<Feed> feedOptions;

    public QueueFeedFilterDialog(List<FeedItem> queue) {
        feedOptions = QueueUtils.getFeedsFromFeedItems(queue);
        filteredFeedIds = new HashSet<>(QueuePreferences.getFeedsFilter());
    }

    public void show(FragmentManager fragmentManager) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new ChecklistDialog<Feed>(
                feedOptions,
                (index) -> {
                    return feedOptions.get((Integer) index).getTitle();
                },
                (index) -> {
                    return filteredFeedIds.contains(feedOptions.get((Integer) index).getId());
                },
                (index, isChecked) -> {
                    Log.d(TAG, "Podcast filter: index " + index + ", isChecked " + isChecked);
                    Feed selectedFeed = feedOptions.get(index);
                    if (isChecked) {
                        filteredFeedIds.add(selectedFeed.getId());
                    } else {
                        filteredFeedIds.remove(selectedFeed.getId());
                    }
                },
                (dialog, which) -> {
                    Log.d(TAG, "Applying podcast filter");
                    for (Long feedId : filteredFeedIds) {
                        Log.d(TAG, "Filtered feed id: " + feedId);
                    }

                    QueuePreferences.setFeedsFilter(filteredFeedIds.toArray(new Long[0]));
                    boolean isFiltered = !filteredFeedIds.isEmpty();
                    EventBus.getDefault().post(QueueEvent.filtered(isFiltered));
                },
                (dialog, which) -> {
                    Log.d(TAG, "Canceling podcast filter");
                });
        fragmentTransaction.add(fragment, null);
        fragmentTransaction.commit();
    }
}
