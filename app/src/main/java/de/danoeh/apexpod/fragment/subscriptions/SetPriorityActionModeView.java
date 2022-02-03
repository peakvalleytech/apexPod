package de.danoeh.apexpod.fragment.subscriptions;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

import de.danoeh.antennapod.adapter.FeedsItemMoveCallback;

public class SetPriorityActionModeView implements FeedsItemMoveCallback.ItemTouchHelperContract{
    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
//        Log.d("RowMoved", "from position " + fromPosition + " to position " + toPosition);
//        if (fromPosition < toPosition) {
//            for (int i = fromPosition; i < toPosition; i++) {
//                Collections.swap(listItems, i, i + 1);
//            }
//        } else {
//            for (int i = fromPosition; i > toPosition; i--) {
//                Collections.swap(listItems, i, i - 1);
//            }
//        }
//        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(RecyclerView.ViewHolder myViewHolder) {
    }

    @Override
    public void onRowClear(RecyclerView.ViewHolder myViewHolder) {

    }
}
