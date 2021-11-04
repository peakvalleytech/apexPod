package de.danoeh.apexpod.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
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
import de.danoeh.apexpod.core.storage.repository.PlaylistRepository;
import de.danoeh.apexpod.core.storage.repository.impl.PlaylistRepositoryImpl;
import de.danoeh.apexpod.core.util.FeedItemUtil;
import de.danoeh.apexpod.menuhandler.FeedItemMenuHandler;
import de.danoeh.apexpod.model.Playlist;
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

    private List<Playlist> playlists = new ArrayList<>();
    private PlayListsListAdapter adapter;
    private Disposable disposable;
    private RecyclerView recyclerView;
    private EmptyViewHandler emptyView;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private boolean displayUpArrow;

    private PlaylistRepository playlistRepository = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistRepository =  new PlaylistRepositoryImpl(getContext());
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.simple_list_fragment, container, false);
        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.playlists_label);
//        toolbar.setOnMenuItemClickListener(this);
        displayUpArrow = getParentFragmentManager().getBackStackEntryCount() != 0;
        if (savedInstanceState != null) {
            displayUpArrow = savedInstanceState.getBoolean(KEY_UP_ARROW);
        }
        ((MainActivity) getActivity()).setupToolbarToggle(toolbar, displayUpArrow);
        toolbar.inflateMenu(R.menu.playback_history);
        refreshToolbarState();

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setRecycledViewPool(((MainActivity) getActivity()).getRecycledViewPool());
        adapter = new PlayListsListAdapter(playlists);
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

    public void refreshToolbarState() {
        boolean hasHistory = playlists != null && !playlists.isEmpty();
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.clear_history_item) {
            DBWriter.clearPlaybackHistory();
            return true;
        }
        return false;
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
                    playlists = result;
                    adapter.playlists = playlists;

                    adapter.notifyDataSetChanged();
                    refreshToolbarState();
                    onFragmentLoaded();
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    @NonNull
    private List<Playlist> loadData() {
//        List<Playlist> playlists = playlistRepository.getPlaylists();
        List<Playlist> playlists = new ArrayList<>();
        playlists.add(new Playlist("Playlist 1"));
        playlists.add(new Playlist("Playlist 2"));
        playlists.add(new Playlist("Playlist 3"));
        playlists.add(new Playlist("Playlist 4"));
//        DBReader.loadAdditionalFeedItemListData(PlaylistFragment.this.playlists);
        return playlists;
    }

    private static class PlayListsListAdapter extends RecyclerView.Adapter<PlayListsListAdapter.PlayListsListViewHolder> {
        List<Playlist> playlists;
        public PlayListsListAdapter(List<Playlist> playlists) {
            super();
            this.playlists = playlists;
        }

        @NonNull
        @Override
        public PlayListsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_playlist, parent, false);
            return new PlayListsListViewHolder(root);
        }

        @Override
        public void onBindViewHolder(@NonNull PlayListsListViewHolder holder, int position) {
            holder.nameTextView.setText(playlists.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return playlists.size();
        }

        class PlayListsListViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            public PlayListsListViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.name);
            }
        }

    }
}
