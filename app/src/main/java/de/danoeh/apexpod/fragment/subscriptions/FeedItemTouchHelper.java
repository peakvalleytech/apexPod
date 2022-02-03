package de.danoeh.apexpod.fragment.subscriptions;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

import de.danoeh.antennapod.adapter.FeedsItemMoveCallback;
import de.danoeh.apexpod.adapter.SubscriptionsRecyclerAdapter;

public class FeedItemTouchHelper implements FeedsItemMoveCallback.ItemTouchHelperContract{
    private SubscriptionsRecyclerAdapter adapter;

    public FeedItemTouchHelper(SubscriptionsRecyclerAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        Log.d("RowMoved", "from position " + fromPosition + " to position " + toPosition);
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                adapter.swap(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                adapter.swap(i, i - 1);
            }
        }
        adapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(RecyclerView.ViewHolder myViewHolder) {
    }

    @Override
    public void onRowClear(RecyclerView.ViewHolder myViewHolder) {

    }
}
