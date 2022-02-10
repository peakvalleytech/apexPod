package de.danoeh.apexpod.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.NavDrawerData;
import de.danoeh.apexpod.fragment.subscriptions.SubscriptionViewHolder;
import de.danoeh.apexpod.model.feed.Feed;

/**
 * Adapter for subscriptions
 */
public class SubscriptionsRecyclerAdapter
        extends SelectableAdapter<SubscriptionViewHolder>
        implements View.OnCreateContextMenuListener {
    private static final String TAG = "SubscriptionsRecyclerAdapter";
    private final WeakReference<MainActivity> mainActivityRef;
    private List<NavDrawerData.DrawerItem> listItems;
    private Feed selectedFeed = null;
    int longPressedPosition = 0; // used to init actionMode
    ActionMode setPriorityActionMode;
    // Experimental
    private boolean dragNDropMode = false;
    private StartDragListener startDragListener;
    public void setDragNDropMode(boolean dragNDropMode) {
        this.dragNDropMode = dragNDropMode;
        if(dragNDropMode)
       setPriorityActionMode = mainActivityRef.get().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                mode.setTitle("Set Priority");
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                setDragNDropMode(false);
                notifyDataSetChanged();
            }
        });
    }

    public boolean isDragNDropMode() {
        return dragNDropMode;
    }

    public SubscriptionsRecyclerAdapter(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivityRef = new WeakReference<>(mainActivity);
        this.listItems = new ArrayList<>();
        setHasStableIds(true);
    }

    public Object getItem(int position) {
        return listItems.get(position);
    }

    public Feed getSelectedFeed() {
        return selectedFeed;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mainActivityRef.get()).inflate(R.layout.subscription_item, parent, false);
        return new SubscriptionViewHolder(this.mainActivityRef, itemView, isDragNDropMode(), inActionMode());
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        NavDrawerData.DrawerItem drawerItem = listItems.get(position);
        holder.bind(drawerItem);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public long getItemId(int position) {
        return listItems.get(position).id;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (selectedFeed != null && !inActionMode()) {
            MenuInflater inflater = mainActivityRef.get().getMenuInflater();
            inflater.inflate(R.menu.nav_feed_context, menu);
            menu.setHeaderTitle(selectedFeed.getTitle());
            menu.findItem(R.id.multi_select).setVisible(true);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.multi_select) {
            startSelectMode(longPressedPosition);
            return true;
        }
        return false;
    }

    public List<Feed> getSelectedItems() {
        List<Feed> items = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            if (isSelected(i)) {
                NavDrawerData.DrawerItem drawerItem = listItems.get(i);
                if (drawerItem.type == NavDrawerData.DrawerItem.Type.FEED) {
                    Feed feed = ((NavDrawerData.FeedDrawerItem) drawerItem).feed;
                    items.add(feed);
                }
            }
        }
        return items;
    }

    public void setItems(List<NavDrawerData.DrawerItem> listItems) {
        this.listItems = listItems;
        notifyDataSetChanged();
    }

    @Override
    public void setSelected(int pos, boolean selected) {
        NavDrawerData.DrawerItem drawerItem = listItems.get(pos);
        if (drawerItem.type == NavDrawerData.DrawerItem.Type.FEED) {
            super.setSelected(pos, selected);
        }
    }



    public static float convertDpToPixel(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }



    public void setStartDragListener(StartDragListener startDragListener) {
        this.startDragListener = startDragListener;
    }

    public void requestDrag(SubscriptionViewHolder holder) {
        startDragListener.requestDrag(holder);
    }

    public void swap(int i, int j) {
        Collections.swap(listItems, i, j);
    }

    /**
     * Sets the priority of feed changed through drag and drop
     * @param fromPos
     * @param toPos
     */
    public void swapPriorities(int fromPos, int toPos) {
        Log.d(TAG, "Swapping priorities: from: " + fromPos + " to: " + toPos);
        Feed fromFeed = ((NavDrawerData.FeedDrawerItem) listItems.get(fromPos)).feed;
        Feed toFeed = ((NavDrawerData.FeedDrawerItem) listItems.get(toPos)).feed;
        DBWriter.swapPriorities(fromFeed, toFeed);
    }

    public interface StartDragListener {
        void requestDrag(RecyclerView.ViewHolder viewHolder);
    }

    public static class GridDividerItemDecorator extends RecyclerView.ItemDecoration {
        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.onDraw(c, parent, state);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect,
                                   @NonNull View view,
                                   @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            Context context = parent.getContext();
            int insetOffset = (int) convertDpToPixel(context, 1f);
            outRect.set(insetOffset, insetOffset, insetOffset, insetOffset);
        }
    }

    public void showContextMenu(int longPressedPosition) {
        this.selectedFeed =((NavDrawerData.FeedDrawerItem) getItem(longPressedPosition)).feed;
        this.longPressedPosition = longPressedPosition;
    }

}
