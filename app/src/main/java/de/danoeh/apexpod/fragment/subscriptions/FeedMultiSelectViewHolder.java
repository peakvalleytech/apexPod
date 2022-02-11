package de.danoeh.apexpod.fragment.subscriptions;

import android.view.View;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.adapter.SubscriptionsRecyclerAdapter;

public class FeedMultiSelectViewHolder extends SubscriptionViewHolder {
    public FeedMultiSelectViewHolder(WeakReference<MainActivity> mainActivityRef, @NonNull View itemView) {
        super(mainActivityRef, itemView, false, false);
    }


}
