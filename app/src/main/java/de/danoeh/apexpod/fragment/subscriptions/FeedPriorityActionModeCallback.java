package de.danoeh.apexpod.fragment.subscriptions;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import de.danoeh.apexpod.adapter.SubscriptionsRecyclerAdapter;

public class FeedPriorityActionModeCallback implements ActionMode.Callback {
    SubscriptionsRecyclerAdapter subscriptionsRecyclerAdapter;
    public FeedPriorityActionModeCallback() {
    }

    public FeedPriorityActionModeCallback(SubscriptionsRecyclerAdapter adapter) {
        this.subscriptionsRecyclerAdapter = adapter;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
//        subscriptionsRecyclerAdapter.endSetPriority();
    }
}