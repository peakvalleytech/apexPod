package de.danoeh.apexpod.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.ViewCompat;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.fragment.swipeactions.SwipeActions;
import de.danoeh.apexpod.view.viewholder.EpisodeItemViewHolder;

/**
 * List adapter for the queue.
 */
public class PlayListItemRecyclerAdapter extends EpisodeItemListAdapter {
    private static final String TAG = "QueueRecyclerAdapter";

    private boolean dragDropEnabled;


    public PlayListItemRecyclerAdapter(MainActivity mainActivity) {
        super(mainActivity);
        dragDropEnabled = ! (UserPreferences.isQueueKeepSorted() || UserPreferences.isQueueLocked());
    }

    public void updateDragDropEnabled() {
        dragDropEnabled = ! (UserPreferences.isQueueKeepSorted() || UserPreferences.isQueueLocked());
        notifyDataSetChanged();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void afterBindViewHolder(EpisodeItemViewHolder holder, int pos) {
//        if (!dragDropEnabled || inActionMode()) {
            holder.dragHandle.setVisibility(View.GONE);
            holder.dragHandle.setOnTouchListener(null);
//        } else {
            holder.dragHandle.setOnTouchListener((v1, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "startDrag()");
                }
                return false;
            });
            holder.coverHolder.setOnTouchListener((v1, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    boolean isLtr = ViewCompat.getLayoutDirection(holder.itemView) == ViewCompat.LAYOUT_DIRECTION_LTR;
                    float factor = isLtr ? 1 : -1;
                    if (factor * event.getX() < factor * 0.5 * v1.getWidth()) {
                        Log.d(TAG, "startDrag()");
                    } else {
                        Log.d(TAG, "Ignoring drag in right half of the image");
                    }
                }
                return false;
            });
//        }
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.queue_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.findItem(R.id.move_to_top_item).setVisible(false);
        menu.findItem(R.id.move_to_bottom_item).setVisible(false);
        menu.findItem(R.id.reset_position).setVisible(false);
        menu.findItem(R.id.multi_select).setVisible(false);

        if (!inActionMode()) {
            menu.findItem(R.id.multi_select).setVisible(true);
            final boolean keepSorted = UserPreferences.isQueueKeepSorted();
            if (getItem(0).getId() == getLongPressedItem().getId() || keepSorted) {
                menu.findItem(R.id.move_to_top_item).setVisible(false);
            }
            if (getItem(getItemCount() - 1).getId() == getLongPressedItem().getId() || keepSorted) {
                menu.findItem(R.id.move_to_bottom_item).setVisible(false);
            }
        } else {
            menu.findItem(R.id.move_to_top_item).setVisible(false);
            menu.findItem(R.id.move_to_bottom_item).setVisible(false);
        }
    }
}
