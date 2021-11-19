package de.danoeh.apexpod.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.Callable;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.adapter.EpisodeItemListAdapter;
import de.danoeh.apexpod.adapter.PlayListItemRecyclerAdapter;
import de.danoeh.apexpod.core.dialog.ConfirmationDialog;
import de.danoeh.apexpod.core.event.DownloadEvent;
import de.danoeh.apexpod.core.event.DownloaderUpdate;
import de.danoeh.apexpod.core.event.FeedItemEvent;
import de.danoeh.apexpod.core.event.PlaybackPositionEvent;
import de.danoeh.apexpod.core.event.PlayerStatusEvent;
import de.danoeh.apexpod.core.event.UnreadItemsUpdateEvent;
import de.danoeh.apexpod.core.menuhandler.MenuItemUtils;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.service.download.DownloadService;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.DownloadRequester;
import de.danoeh.apexpod.core.storage.database.PlayListItemDao;
import de.danoeh.apexpod.core.storage.repository.PlaylistRepository;
import de.danoeh.apexpod.core.storage.repository.impl.PlaylistRepositoryImpl;
import de.danoeh.apexpod.core.util.FeedItemUtil;
import de.danoeh.apexpod.core.util.download.AutoUpdateManager;
import de.danoeh.apexpod.fragment.actions.EpisodeMultiSelectActionHandler;
import de.danoeh.apexpod.menuhandler.FeedItemMenuHandler;
import de.danoeh.apexpod.model.Playlist;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.SortOrder;
import de.danoeh.apexpod.view.EmptyViewHandler;
import de.danoeh.apexpod.view.EpisodeItemListRecyclerView;
import de.danoeh.apexpod.view.viewholder.EpisodeItemViewHolder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Shows all items in the queue.
 */
public class PlayListItemFragment extends Fragment implements
        EpisodeItemListAdapter.OnSelectModeListener {
    public static final String TAG = "PlayListItemFragment";
    private static final String KEY_UP_ARROW = "up_arrow";

    private TextView infoBar;
    private EpisodeItemListRecyclerView recyclerView;
    private PlayListItemRecyclerAdapter recyclerAdapter;
    private EmptyViewHandler emptyView;
    private ProgressBar progLoading;
    private Toolbar toolbar;
    private boolean displayUpArrow;

    private Playlist playList;
    private List<FeedItem> playListItems;

    private boolean isUpdatingFeeds = false;

    private static final String PREFS = "QueueFragment";
    private static final String PREF_SHOW_LOCK_WARNING = "show_lock_warning";
    public static final String ARG_PLAYLIST = "playlist";

    private Disposable disposable;
    private SharedPreferences prefs;

    PlaylistRepository playlistRepository;
    PlayListItemDao playListItemDao;
    private SpeedDialView speedDialView;
    public static PlayListItemFragment newInstance(Playlist playlist) {
        PlayListItemFragment fragment = new PlayListItemFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAYLIST, playlist);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete_playlist) {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog(getContext(), R.string.delete_playlist, R.string.delete_playlist_confirmation_msg) {
                @Override
                public void onConfirmButtonPressed(DialogInterface dialog) {
                    if (playlistRepository != null) {
                        playlistRepository.deletePlaylist(playList.getId());
                        for (FeedItem feedItem : playListItems) {
                            playListItemDao.deleteItemByPlayListId(playList.getId(), feedItem);
                        }
                    }
                }
            };

            confirmationDialog.createNewDialog().show();
        }
        return true;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playback_history, menu);
//        menu.findItem(R.id.delete_playlist)
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        playList = (Playlist) getArguments().getSerializable(ARG_PLAYLIST);
        prefs = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        playlistRepository = new PlaylistRepositoryImpl(getContext());
        playListItemDao = new PlayListItemDao();
        hasOptionsMenu();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playListItems != null) {
            onFragmentLoaded(true);
        }
        loadItems(true);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        recyclerView.saveScrollPosition(PlayListItemFragment.TAG);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FeedItemEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        if (playListItems == null) {
            return;
        } else if (recyclerAdapter == null) {
            loadItems(true);
            return;
        }
        for (int i = 0, size = event.items.size(); i < size; i++) {
            FeedItem item = event.items.get(i);
            int pos = FeedItemUtil.indexOfItemWithId(playListItems, item.getId());
            if (pos >= 0) {
                playListItems.remove(pos);
                playListItems.add(pos, item);
                recyclerAdapter.notifyItemChangedCompat(pos);
                refreshInfoBar();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadEvent event) {
        Log.d(TAG, "onEventMainThread() called with DownloadEvent");
        DownloaderUpdate update = event.update;
        if (event.hasChangedFeedUpdateStatus(isUpdatingFeeds)) {
            refreshToolbarState();
        }
        if (recyclerAdapter != null && update.mediaIds.length > 0) {
            for (long mediaId : update.mediaIds) {
                int pos = FeedItemUtil.indexOfItemWithMediaId(playListItems, mediaId);
                if (pos >= 0) {
                    recyclerAdapter.notifyItemChangedCompat(pos);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PlaybackPositionEvent event) {
        if (recyclerAdapter != null) {
            for (int i = 0; i < recyclerAdapter.getItemCount(); i++) {
                EpisodeItemViewHolder holder = (EpisodeItemViewHolder)
                        recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null && holder.isCurrentlyPlayingItem()) {
                    holder.notifyPlaybackPositionUpdated(event);
                    break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusChanged(PlayerStatusEvent event) {
        loadItems(false);
        if (isUpdatingFeeds != updateRefreshMenuItemChecker.isRefreshing()) {
            refreshToolbarState();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadItemsChanged(UnreadItemsUpdateEvent event) {
        // Sent when playback position is reset
        loadItems(false);
        if (isUpdatingFeeds != updateRefreshMenuItemChecker.isRefreshing()) {
            refreshToolbarState();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recyclerAdapter != null) {
            recyclerAdapter.endSelectMode();
        }
        recyclerAdapter = null;
    }

    private final MenuItemUtils.UpdateRefreshMenuItemChecker updateRefreshMenuItemChecker =
            () -> DownloadService.isRunning && DownloadRequester.getInstance().isDownloadingFeeds();

    private void refreshToolbarState() {
//        toolbar.getMenu().findItem(R.id.queue_lock).setChecked(UserPreferences.isQueueLocked());
//        boolean keepSorted = UserPreferences.isQueueKeepSorted();
//        toolbar.getMenu().findItem(R.id.queue_sort_random).setVisible(!keepSorted);
//        toolbar.getMenu().findItem(R.id.queue_keep_sorted).setChecked(keepSorted);
//        isUpdatingFeeds = MenuItemUtils.updateRefreshMenuItem(toolbar.getMenu(),
//                R.id.refresh_item, updateRefreshMenuItemChecker);
    }


//    public boolean onMenuItemClick(MenuItem item) {
//        final int itemId = item.getItemId();
//       if (itemId == R.id.queue_sort_episode_title_asc) {
//            setSortOrder(SortOrder.EPISODE_TITLE_A_Z);
//            return true;
//        } else if (itemId == R.id.queue_sort_episode_title_desc) {
//            setSortOrder(SortOrder.EPISODE_TITLE_Z_A);
//            return true;
//        } else if (itemId == R.id.queue_sort_date_asc) {
//            setSortOrder(SortOrder.DATE_OLD_NEW);
//            return true;
//        } else if (itemId == R.id.queue_sort_date_desc) {
//            setSortOrder(SortOrder.DATE_NEW_OLD);
//            return true;
//        } else if (itemId == R.id.queue_sort_duration_asc) {
//            setSortOrder(SortOrder.DURATION_SHORT_LONG);
//            return true;
//        } else if (itemId == R.id.queue_sort_duration_desc) {
//            setSortOrder(SortOrder.DURATION_LONG_SHORT);
//            return true;
//        } else if (itemId == R.id.queue_sort_feed_title_asc) {
//            setSortOrder(SortOrder.FEED_TITLE_A_Z);
//            return true;
//        } else if (itemId == R.id.queue_sort_feed_title_desc) {
//            setSortOrder(SortOrder.FEED_TITLE_Z_A);
//            return true;
//        } else if (itemId == R.id.queue_sort_random) {
//            setSortOrder(SortOrder.RANDOM);
//            return true;
//        } else if (itemId == R.id.queue_sort_smart_shuffle_asc) {
//            setSortOrder(SortOrder.SMART_SHUFFLE_OLD_NEW);
//            return true;
//        } else if (itemId == R.id.queue_sort_smart_shuffle_desc) {
//            setSortOrder(SortOrder.SMART_SHUFFLE_NEW_OLD);
//            return true;
//        } else if (itemId == R.id.queue_keep_sorted) {
//            boolean keepSortedOld = UserPreferences.isQueueKeepSorted();
//            boolean keepSortedNew = !keepSortedOld;
//            UserPreferences.setQueueKeepSorted(keepSortedNew);
//            if (keepSortedNew) {
//                SortOrder sortOrder = UserPreferences.getQueueKeepSortedOrder();
//                DBWriter.reorderQueue(sortOrder, true);
//            }
//            if (recyclerAdapter != null) {
//                recyclerAdapter.updateDragDropEnabled();
//            }
//            refreshToolbarState();
//            return true;
//        } else if (itemId == R.id.action_search) {
//            ((MainActivity) getActivity()).loadChildFragment(SearchFragment.newInstance());
//            return true;
//        }
//        return false;
//    }

    /**
     * This method is called if the user clicks on a sort order menu item.
     *
     * @param sortOrder New sort order.
     */
    private void setSortOrder(SortOrder sortOrder) {
        UserPreferences.setQueueKeepSortedOrder(sortOrder);
        DBWriter.reorderQueue(sortOrder, true);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected() called with: " + "item = [" + item + "]");
        if (!isVisible() || recyclerAdapter == null) {
            return false;
        }
        FeedItem selectedItem = recyclerAdapter.getLongPressedItem();
        if (selectedItem == null) {
            Log.i(TAG, "Selected item was null, ignoring selection");
            return super.onContextItemSelected(item);
        }

        int position = FeedItemUtil.indexOfItemWithId(playListItems, selectedItem.getId());
        if (position < 0) {
            Log.i(TAG, "Selected item no longer exist, ignoring selection");
            return super.onContextItemSelected(item);
        }
        if (recyclerAdapter.onContextItemSelected(item)) {
            return true;
        }

        return FeedItemMenuHandler.onMenuItemClicked(this, item.getItemId(), selectedItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.queue_fragment, container, false);
        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle("Playlist");
        displayUpArrow = getParentFragmentManager().getBackStackEntryCount() != 0;
        if (savedInstanceState != null) {
            displayUpArrow = savedInstanceState.getBoolean(KEY_UP_ARROW);
        }
        ((MainActivity) getActivity()).setupToolbarToggle(toolbar, displayUpArrow);
        toolbar.inflateMenu(R.menu.playlists);
        toolbar.getMenu().findItem(R.id.delete_playlist).setOnMenuItemClickListener(item -> {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog(getContext(), R.string.delete_playlist, R.string.delete_playlist_confirmation_msg) {
                @Override
                public void onConfirmButtonPressed(DialogInterface dialog) {
                    if (playlistRepository != null) {
                        playlistRepository.deletePlaylist(playList.getId());
                        for (FeedItem feedItem : playListItems) {
                            playListItemDao.deleteItemByPlayListId(playList.getId(), feedItem);
                        }
                    }
                    ((MainActivity) getActivity()).loadFragment(PlaylistFragment.TAG, savedInstanceState);              }
            };

            confirmationDialog.createNewDialog().show();
            return true;
        });
        refreshToolbarState();

        infoBar = root.findViewById(R.id.info_bar);
        recyclerView = root.findViewById(R.id.recyclerView);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.setRecycledViewPool(((MainActivity) getActivity()).getRecycledViewPool());
        registerForContextMenu(recyclerView);

        SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setDistanceToTriggerSync(getResources().getInteger(R.integer.swipe_refresh_distance));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            AutoUpdateManager.runImmediate(requireContext());
            new Handler(Looper.getMainLooper()).postDelayed(() -> swipeRefreshLayout.setRefreshing(false),
                    getResources().getInteger(R.integer.swipe_to_refresh_duration_in_ms));
        });

        emptyView = new EmptyViewHandler(getContext());
        emptyView.attachToRecyclerView(recyclerView);
        emptyView.setIcon(R.drawable.ic_playlist);
        emptyView.setTitle(R.string.no_items_header_label);
        emptyView.setMessage(R.string.no_items_label);

        progLoading = root.findViewById(R.id.progLoading);
        progLoading.setVisibility(View.VISIBLE);

        speedDialView = root.findViewById(R.id.fabSD);
        speedDialView.setOverlayLayout(root.findViewById(R.id.fabSDOverlay));
        speedDialView.inflate(R.menu.episodes_apply_action_speeddial);
        speedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false;
            }

            @Override
            public void onToggleChanged(boolean open) {
                if (open && recyclerAdapter.getSelectedCount() == 0) {
                    ((MainActivity) getActivity()).showSnackbarAbovePlayer(R.string.no_items_selected,
                            Snackbar.LENGTH_SHORT);
                    speedDialView.close();
                }
            }
        });
        speedDialView.setOnActionSelectedListener(actionItem -> {
            new EpisodeMultiSelectActionHandler(((MainActivity) getActivity()), recyclerAdapter.getSelectedItems())
                    .handleAction(actionItem.getId());
            recyclerAdapter.endSelectMode();
            return true;
        });
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_UP_ARROW, displayUpArrow);
        super.onSaveInstanceState(outState);
    }

    private void onFragmentLoaded(final boolean restoreScrollPosition) {
        if (playListItems != null) {
            if (recyclerAdapter == null) {
                MainActivity activity = (MainActivity) getActivity();
                recyclerAdapter = new PlayListItemRecyclerAdapter(activity);
                recyclerAdapter.setOnSelectModeListener(this);
                recyclerView.setAdapter(recyclerAdapter);
                emptyView.updateAdapter(recyclerAdapter);
            }
            recyclerAdapter.updateItems(playListItems);
        } else {
            recyclerAdapter = null;
            emptyView.updateAdapter(null);
        }

        if (restoreScrollPosition) {
            recyclerView.restoreScrollPosition(PlayListItemFragment.TAG);
        }

        // we need to refresh the options menu because it sometimes
        // needs data that may have just been loaded.
        refreshToolbarState();

        refreshInfoBar();
    }

    private void refreshInfoBar() {
        int numOfEpisodes = playListItems.size();
        String subtitle = playList.getName() + " - " + getResources()
                .getQuantityString(R.plurals.num_episodes, numOfEpisodes, numOfEpisodes);
        infoBar.setText(subtitle);
    }

    private void loadItems(final boolean restoreScrollPosition) {
        Log.d(TAG, "loadItems()");
        if (disposable != null) {
            disposable.dispose();
        }
        if (playListItems == null) {
            emptyView.hide();
            progLoading.setVisibility(View.VISIBLE);
        }
        PlayListItemDao playListItemDao = new PlayListItemDao();
        disposable = Observable.fromCallable(new Callable<List<FeedItem>>() {
            @Override
            public List<FeedItem> call() throws Exception {
                return playListItemDao.getItemsByPlayListId(playList.getId());
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    progLoading.setVisibility(View.GONE);
                    playListItems = items;
                    onFragmentLoaded(restoreScrollPosition);
                    if (recyclerAdapter != null) {
                        recyclerAdapter.notifyDataSetChanged();
                    }
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    @Override
    public void onStartSelectMode() {
        speedDialView.setVisibility(View.VISIBLE);
        refreshToolbarState();
        infoBar.setVisibility(View.GONE);
    }

    @Override
    public void onEndSelectMode() {
        speedDialView.close();
        speedDialView.setVisibility(View.GONE);
        infoBar.setVisibility(View.VISIBLE);
    }
}
