package de.danoeh.antennapod.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import de.danoeh.apexpod.adapter.SubscriptionsRecyclerAdapter;
import de.danoeh.apexpod.fragment.subscriptions.SubscriptionViewHolder;

public class DragAndDropItemTouchHelper extends ItemTouchHelper.Callback {
    private final SubscriptionsRecyclerAdapter adapter;

    public DragAndDropItemTouchHelper(SubscriptionsRecyclerAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getBindingAdapterPosition();
        int toPosition = target.getBindingAdapterPosition();
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
        return true;
    }


    @Override
    public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {}

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }
}
