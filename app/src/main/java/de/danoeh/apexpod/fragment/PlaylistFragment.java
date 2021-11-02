package de.danoeh.apexpod.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.adapter.EpisodeItemListAdapter;
import de.danoeh.apexpod.core.event.DownloadEvent;
import de.danoeh.apexpod.core.event.DownloaderUpdate;
import de.danoeh.apexpod.core.event.FeedItemEvent;
import de.danoeh.apexpod.core.event.PlaybackHistoryEvent;
import de.danoeh.apexpod.core.event.PlaybackPositionEvent;
import de.danoeh.apexpod.core.event.PlayerStatusEvent;
import de.danoeh.apexpod.core.event.UnreadItemsUpdateEvent;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.util.FeedItemUtil;
import de.danoeh.apexpod.menuhandler.FeedItemMenuHandler;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.view.EmptyViewHandler;
import de.danoeh.apexpod.view.EpisodeItemListRecyclerView;
import de.danoeh.apexpod.view.viewholder.EpisodeItemViewHolder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlaylistFragment extends Fragment {
    public static final String TAG = "PlaylistFragment";
    private static final String KEY_UP_ARROW = "up_arrow";

    private List<FeedItem> playlists;
    private PlayListsListAdapter adapter;
    private Disposable disposable;
    private EpisodeItemListRecyclerView recyclerView;
    private EmptyViewHandler emptyView;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private boolean displayUpArrow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.simple_list_fragment, container, false);
        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.playback_history_label);
        toolbar.setOnMenuItemClickListener(this);
        displayUpArrow = getParentFragmentManager().getBackStackEntryCount() != 0;
        if (savedInstanceState != null) {
            displayUpArrow = savedInstanceState.getBoolean(KEY_UP_ARROW);
        }
        ((MainActivity) getActivity()).setupToolbarToggle(toolbar, displayUpArrow);
        toolbar.inflateMenu(R.menu.playback_history);
        refreshToolbarState();

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setRecycledViewPool(((MainActivity) getActivity()).getRecycledViewPool());
        adapter = new PlayListsListAdapter((MainActivity) getActivity());
        recyclerView.setAdapter(adapter);
        progressBar = root.findViewById(R.id.progLoading);

        emptyView = new EmptyViewHandler(getActivity());
        emptyView.setIcon(R.drawable.ic_history);
        emptyView.setTitle(R.string.no_history_head_label);
        emptyView.setMessage(R.string.no_history_label);
        emptyView.attachToRecyclerView(recyclerView);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        loadItems();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_UP_ARROW, displayUpArrow);
        super.onSaveInstanceState(outState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FeedItemEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        if (playbackHistory == null) {
            return;
        } else if (adapter == null) {
            loadItems();
            return;
        }
        for (int i = 0, size = event.items.size(); i < size; i++) {
            FeedItem item = event.items.get(i);
            int pos = FeedItemUtil.indexOfItemWithId(playbackHistory, item.getId());
            if (pos >= 0) {
                playbackHistory.remove(pos);
                playbackHistory.add(pos, item);
                adapter.notifyItemChangedCompat(pos);
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        DownloaderUpdate update = event.update;
        if (adapter != null && update.mediaIds.length > 0) {
            for (long mediaId : update.mediaIds) {
                int pos = FeedItemUtil.indexOfItemWithMediaId(playbackHistory, mediaId);
                if (pos >= 0) {
                    adapter.notifyItemChangedCompat(pos);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PlaybackPositionEvent event) {
        if (adapter != null) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                EpisodeItemViewHolder holder = (EpisodeItemViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null && holder.isCurrentlyPlayingItem()) {
                    holder.notifyPlaybackPositionUpdated(event);
                    break;
                }
            }
        }
    }

    public void refreshToolbarState() {
        boolean hasHistory = playbackHistory != null && !playbackHistory.isEmpty();
        toolbar.getMenu().findItem(R.id.clear_history_item).setVisible(hasHistory);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.clear_history_item) {
            DBWriter.clearPlaybackHistory();
            return true;
        }
        return false;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        FeedItem selectedItem = adapter.getLongPressedItem();
        if (selectedItem == null) {
            Log.i(TAG, "Selected item at current position was null, ignoring selection");
            return super.onContextItemSelected(item);
        }
        return FeedItemMenuHandler.onMenuItemClicked(this, item.getItemId(), selectedItem);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHistoryUpdated(PlaybackHistoryEvent event) {
        loadItems();
        refreshToolbarState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusChanged(PlayerStatusEvent event) {
        loadItems();
        refreshToolbarState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadItemsChanged(UnreadItemsUpdateEvent event) {
        loadItems();
        refreshToolbarState();
    }

    private void onFragmentLoaded() {
        adapter.notifyDataSetChanged();
        refreshToolbarState();
    }

    private void loadItems() {
        if (disposable != null) {
            disposable.dispose();
        }
        progressBar.setVisibility(View.VISIBLE);
        emptyView.hide();
        disposable = Observable.fromCallable(this::loadData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    progressBar.setVisibility(View.GONE);
                    playbackHistory = result;
                    adapter.updateItems(playbackHistory);
                    onFragmentLoaded();
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    @NonNull
    private List<FeedItem> loadData() {
        List<FeedItem> history = DBReader.getPlaybackHistory();
        DBReader.loadAdditionalFeedItemListData(history);
        return history;
    }

    private static class PlayListsListAdapter extends RecyclerView.Adapter<PlayListsListAdapter.PlayListsListViewHolder> {

        public PlayListsListAdapter(MainActivity mainActivity) {
            super(mainActivity);
        }

        @NonNull
        @Override
        public PlayListsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull PlayListsListViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        class PlayListsListViewHolder extends RecyclerView.ViewHolder {

            public PlayListsListViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

    }
}
