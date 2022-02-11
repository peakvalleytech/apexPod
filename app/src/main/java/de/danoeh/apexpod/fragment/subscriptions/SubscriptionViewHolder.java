package de.danoeh.apexpod.fragment.subscriptions;

import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Locale;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.adapter.CoverLoader;
import de.danoeh.apexpod.adapter.SubscriptionsRecyclerAdapter;
import de.danoeh.apexpod.core.feed.LocalFeedUpdater;
import de.danoeh.apexpod.core.storage.NavDrawerData;
import de.danoeh.apexpod.fragment.FeedItemlistFragment;
import de.danoeh.apexpod.model.feed.Feed;
import jp.shts.android.library.TriangleLabelView;

public class SubscriptionViewHolder extends RecyclerView.ViewHolder {
    private final WeakReference<MainActivity> mainActivityRef;
    public TextView feedTitle;
    public ImageView imageView;
    public TriangleLabelView count;
    public FrameLayout selectView;
    public CheckBox selectCheckbox;
    public ImageView dragHandle;
    SubscriptionsRecyclerAdapter adapter;
    public SubscriptionViewHolder(WeakReference<MainActivity> mainActivityRef, @NonNull View itemView, boolean dragNDropMode, boolean inActionMode) {
        super(itemView);
        this.mainActivityRef = mainActivityRef;
        feedTitle = itemView.findViewById(R.id.txtvTitle);
        imageView = itemView.findViewById(R.id.imgvCover);
        count = itemView.findViewById(R.id.triangleCountView);
        selectView = itemView.findViewById(R.id.selectView);
        selectCheckbox = itemView.findViewById(R.id.selectCheckBox);
        dragHandle = itemView.findViewById(R.id.dragHandle);
    }

    public void bind(NavDrawerData.DrawerItem drawerItem) {
        adapter = (SubscriptionsRecyclerAdapter) getBindingAdapter();
        feedTitle.setText(drawerItem.getTitle());
        imageView.setContentDescription(drawerItem.getTitle());
        feedTitle.setVisibility(View.VISIBLE);
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
                == ViewCompat.LAYOUT_DIRECTION_RTL) {
            count.setCorner(TriangleLabelView.Corner.TOP_LEFT);
        }

        if (drawerItem.getCounter() > 0) {
            count.setPrimaryText(NumberFormat.getInstance().format(drawerItem.getCounter()));
            count.setVisibility(View.VISIBLE);
        } else {
            count.setVisibility(View.GONE);
        }

        if (drawerItem.type == NavDrawerData.DrawerItem.Type.FEED) {
            Feed feed = ((NavDrawerData.FeedDrawerItem) drawerItem).feed;
            boolean textAndImageCombind = feed.isLocalFeed()
                    && LocalFeedUpdater.getDefaultIconUrl(itemView.getContext()).equals(feed.getImageUrl());
            new CoverLoader(mainActivityRef.get())
                    .withUri(feed.getImageUrl())
                    .withPlaceholderView(feedTitle, textAndImageCombind)
                    .withCoverView(imageView)
                    .load();
        } else {
            new CoverLoader(mainActivityRef.get())
                    .withResource(R.drawable.ic_tag)
                    .withPlaceholderView(feedTitle, true)
                    .withCoverView(imageView)
                    .load();
        }
        if(!adapter.isDragNDropMode()) {
            itemView.setOnCreateContextMenuListener(adapter);
            selectView.setVisibility(View.GONE);
            dragHandle.setVisibility(View.GONE);

        }
        else {
            itemView.setOnCreateContextMenuListener(null);
            selectView.setVisibility(View.VISIBLE);
            dragHandle.setVisibility(View.VISIBLE);
            selectCheckbox.setVisibility(View.GONE);
            itemView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    adapter.requestDrag(this);
                }
                return false;
            });
        }

        if (adapter.inActionMode()) {
            selectView.setVisibility(View.VISIBLE);
            selectCheckbox.setChecked((adapter.isSelected(getBindingAdapterPosition())));
            selectCheckbox.setOnCheckedChangeListener((buttonView, isChecked)
                    -> adapter.setSelected(getBindingAdapterPosition(), isChecked));
            imageView.setAlpha(0.6f);
            count.setVisibility(View.GONE);
        } else {
            if(!adapter.isDragNDropMode())
                selectView.setVisibility(View.GONE);
            imageView.setAlpha(1.0f);
        }

        itemView.setOnLongClickListener(v -> {
            if (!adapter.inActionMode()) {
                adapter.showContextMenu(getBindingAdapterPosition());
            }
            return false;
        });

        itemView.setOnClickListener(v -> {
            if(!adapter.isDragNDropMode() && !adapter.inActionMode()) {
                Fragment fragment = FeedItemlistFragment.newInstance(((NavDrawerData.FeedDrawerItem) drawerItem).feed.getId());
                mainActivityRef.get().loadChildFragment(fragment);
            } else if (adapter.inActionMode()) {
                adapter.setSelected(getBindingAdapterPosition(), !selectCheckbox.isSelected());
                selectCheckbox.setChecked(!selectCheckbox.isChecked());
            }

        });
    }
}