package de.danoeh.apexpod.fragment.subscriptions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joanzapata.iconify.Iconify;
import com.leinardi.android.speeddial.SpeedDialView;

import de.danoeh.apexpod.adapter.DragAndDropItemTouchHelper;
import de.danoeh.apexpod.adapter.ActionModeCallback;
import de.danoeh.apexpod.dialog.TagSettingsDialog;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import de.danoeh.antennapod.core.feed.TagFilter;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.adapter.FeedTagAdapter;
import de.danoeh.apexpod.adapter.SubscriptionsRecyclerAdapter;
import de.danoeh.apexpod.dialog.ConfirmationDialog;
import de.danoeh.apexpod.core.event.FeedListUpdateEvent;
import de.danoeh.apexpod.core.event.UnreadItemsUpdateEvent;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DBWriter;

import de.danoeh.apexpod.core.event.DownloadEvent;
import de.danoeh.apexpod.core.storage.NavDrawerData;
import de.danoeh.apexpod.core.util.download.AutoUpdateManager;
import de.danoeh.apexpod.dialog.FeedSortDialog;
import de.danoeh.apexpod.dialog.RemoveFeedDialog;
import de.danoeh.apexpod.dialog.RenameFeedDialog;
import de.danoeh.apexpod.dialog.SubscriptionsFilterDialog;
import de.danoeh.apexpod.fragment.AddFeedFragment;
import de.danoeh.apexpod.fragment.SearchFragment;
import de.danoeh.apexpod.fragment.actions.FeedMultiSelectActionHandler;
import de.danoeh.apexpod.model.feed.Feed;
import de.danoeh.apexpod.model.feed.FeedPreferences;
import de.danoeh.apexpod.util.FeedSorter;
import de.danoeh.apexpod.view.EmptyViewHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment for displaying feed subscriptions
 */
public class SubscriptionFragment
        extends
            Fragment
        implements
            Toolbar.OnMenuItemClickListener,
            SubscriptionsRecyclerAdapter.OnSelectModeListener,
        SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String TAG = "SubscriptionFragment";
    private static final String PREFS = "SubscriptionFragment";
    private static final String PREF_NUM_COLUMNS = "columns";
    public static final String PREF_TAG_FILTER = "prefTagFilter";
    private static final String KEY_UP_ARROW = "up_arrow";
    private static final String ARGUMENT_FOLDER = "folder";

    private static final int MIN_NUM_COLUMNS = 2;
    private static final int[] COLUMN_CHECKBOX_IDS = {
            R.id.subscription_num_columns_2,
            R.id.subscription_num_columns_3,
            R.id.subscription_num_columns_4,
            R.id.subscription_num_columns_5};

    private RecyclerView subscriptionRecycler;
    private SubscriptionsRecyclerAdapter subscriptionAdapter;
    private FloatingActionButton subscriptionAddButton;
    private ProgressBar progressBar;
    private EmptyViewHandler emptyView;
    private TextView feedsFilteredMsg;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String displayedFolder = null;
    private boolean isUpdatingFeeds = false;
    private boolean displayUpArrow;

    private Disposable disposable;
    private SharedPreferences prefs;

    private SpeedDialView speedDialView;
    private List<NavDrawerData.DrawerItem> listItems;

    private List<NavDrawerData.DrawerItem> tagFilteredFeeds;
    private NavDrawerData.TagDrawerItem rootFolder;
    private RecyclerView tagRecycler;
    private FeedTagAdapter feedTagAdapter;


    public static SubscriptionFragment newInstance(String folderTitle) {
        SubscriptionFragment fragment = new SubscriptionFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_FOLDER, folderTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        prefs = requireActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        MainActivity activity = (MainActivity) getActivity();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this);
        displayUpArrow = getParentFragmentManager().getBackStackEntryCount() != 0;
        if (savedInstanceState != null) {
            displayUpArrow = savedInstanceState.getBoolean(KEY_UP_ARROW);
        }
        ((MainActivity) getActivity()).setupToolbarToggle(toolbar, displayUpArrow);
        toolbar.inflateMenu(R.menu.subscriptions);
        int currColumnPref = prefs.getInt(PREF_NUM_COLUMNS, getDefaultNumOfColumns());
        for (int i = 0; i < COLUMN_CHECKBOX_IDS.length; i++) {
            // Do this in Java to localize numbers
            MenuItem numColumnsMenuItem = toolbar.getMenu().findItem(COLUMN_CHECKBOX_IDS[i])
                    .setTitle(String.format(Locale.getDefault(), "%d", i + MIN_NUM_COLUMNS));
            if (currColumnPref - 2 == i) {
                numColumnsMenuItem.setChecked(true);
            }
        }

        if (getArguments() != null) {
            displayedFolder = getArguments().getString(ARGUMENT_FOLDER, null);
            if (displayedFolder != null) {
                toolbar.setTitle(displayedFolder);
            }
        }

        subscriptionRecycler = root.findViewById(R.id.subscriptions_grid);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                prefs.getInt(PREF_NUM_COLUMNS, getDefaultNumOfColumns()),
                RecyclerView.VERTICAL,
                false);
        subscriptionRecycler.setLayoutManager(gridLayoutManager);
        subscriptionRecycler.addItemDecoration(new SubscriptionsRecyclerAdapter.GridDividerItemDecorator());
        gridLayoutManager.setSpanCount(prefs.getInt(PREF_NUM_COLUMNS, getDefaultNumOfColumns()));
        registerForContextMenu(subscriptionRecycler);
        subscriptionAddButton = root.findViewById(R.id.subscriptions_add);
        progressBar = root.findViewById(R.id.progLoading);

        feedsFilteredMsg = root.findViewById(R.id.feeds_filtered_message);
        feedsFilteredMsg.setOnClickListener((l) -> SubscriptionsFilterDialog.showDialog(requireContext()));

        swipeRefreshLayout = root.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setDistanceToTriggerSync(getResources().getInteger(R.integer.swipe_refresh_distance));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            AutoUpdateManager.runImmediate(requireContext());
            new Handler(Looper.getMainLooper()).postDelayed(() -> swipeRefreshLayout.setRefreshing(false),
                    getResources().getInteger(R.integer.swipe_to_refresh_duration_in_ms));
        });

        speedDialView = root.findViewById(R.id.fabSD);
        speedDialView.setOverlayLayout(root.findViewById(R.id.fabSDOverlay));
        speedDialView.inflate(R.menu.nav_feed_action_speeddial);
        speedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false;
            }

            @Override
            public void onToggleChanged(boolean isOpen) {}
        });
        speedDialView.setOnActionSelectedListener(actionItem -> {
            new FeedMultiSelectActionHandler((MainActivity) getActivity(), subscriptionAdapter.getSelectedItems())
                    .handleAction(actionItem.getId());
            return true;
        });

        tagRecycler = root.findViewById(R.id.tagRecycler);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        tagRecycler.setLayoutManager(linearLayoutManager);


        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_UP_ARROW, displayUpArrow);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final int itemId = item.getItemId();
       if (itemId == R.id.subscriptions_filter) {
            SubscriptionsFilterDialog.showDialog(requireContext());
            return true;
        } else if (itemId == R.id.subscriptions_sort) {
            FeedSortDialog.showDialog(requireContext());
            return true;
        } else {
           if (itemId == R.id.subscription_num_columns_2) {
               setColumnNumber(2);
           } else if (itemId == R.id.subscription_num_columns_3) {
               setColumnNumber(3);
           } else if (itemId == R.id.subscription_num_columns_4) {
               setColumnNumber(4);
           } else if (itemId == R.id.subscription_num_columns_5) {
               setColumnNumber(5);
           } else if (itemId == R.id.action_search) {
               ((MainActivity) getActivity()).loadChildFragment(SearchFragment.newInstance());
               return true;
           } else {
               return false;
           }
           item.setChecked(true);
           return true;
       }
    }

    private void setColumnNumber(int columns) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
               columns,
                RecyclerView.VERTICAL,
                false);
        subscriptionRecycler.setLayoutManager(gridLayoutManager);
        prefs.edit().putInt(PREF_NUM_COLUMNS, columns).apply();
    }

    private void setupEmptyView() {
        emptyView = new EmptyViewHandler(getContext());
        emptyView.setIcon(R.drawable.ic_folder);
        emptyView.setTitle(R.string.no_subscriptions_head_label);
        emptyView.setMessage(R.string.no_subscriptions_label);
        emptyView.attachToRecyclerView(subscriptionRecycler);
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        subscriptionAdapter = new SubscriptionsRecyclerAdapter((MainActivity) getActivity());
        subscriptionAdapter.setOnSelectModeListener(this);
        subscriptionRecycler.setAdapter(subscriptionAdapter);
        subscriptionAddButton.setOnClickListener(view -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadChildFragment(new AddFeedFragment());
            }
        });
        setupEmptyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        loadSubscriptions();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (disposable != null) {
            disposable.dispose();
        }

        if (subscriptionAdapter != null) {
            subscriptionAdapter.endSelectMode();
            endDragDropMode();
        }
    }

    private void loadSubscriptions() {
        if (disposable != null) {
            disposable.dispose();
        }
        emptyView.hide();
        disposable = Observable.fromCallable(
                () -> {
                    NavDrawerData data = DBReader.getNavDrawerData();
                    List<NavDrawerData.DrawerItem> items = data.items;
                    for (NavDrawerData.DrawerItem item : items) {
                        if (item.type == NavDrawerData.DrawerItem.Type.TAG
                                && item.getTitle().equals(displayedFolder)) {
                            return ((NavDrawerData.TagDrawerItem) item).children;
                        }
                    }
                    return items;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        if (listItems != null && listItems.size() > result.size()) {
                            // We have fewer items. This can result in items being selected that are no longer visible.
                            subscriptionAdapter.endSelectMode();
                        }
                        listItems = result;
                        Pair<List<NavDrawerData.DrawerItem>,
                                List<NavDrawerData.TagDrawerItem>> feedsAndTags =
                                extractFeedsAndTags(result);
                        tagFilteredFeeds = feedsAndTags.first;
                        List<NavDrawerData.TagDrawerItem> tags = feedsAndTags.second;

                        initTagViews(tags);

                        subscriptionAdapter.setItems(sortFeeds(tagFilteredFeeds));
                        subscriptionAdapter.setActionModeCallback(new ActionModeCallback() {
                            @Override
                            public void onStart(int actionModeCode) {
                                if (actionModeCode == SubscriptionsRecyclerAdapter.ACTION_MODE_PRIORITY) {
                                    subscriptionAddButton.setVisibility(View.GONE);
                                    showTagBar(false);
                                }
                            }

                            @Override
                            public void onEnd(int actionModeCode) {
                                if (actionModeCode == SubscriptionsRecyclerAdapter.ACTION_MODE_PRIORITY) {
                                    endDragDropMode();
                                    subscriptionAddButton.setVisibility(View.VISIBLE);
                                    showTagBar(true);
                                }
                            }
                        });
                        emptyView.updateVisibility();
                        progressBar.setVisibility(View.GONE); // Keep hidden to avoid flickering while refreshing
                    }, error -> {
                        Log.e(TAG, Log.getStackTraceString(error));
                        progressBar.setVisibility(View.GONE);
                    });

        if (UserPreferences.getSubscriptionsFilter().isEnabled()) {
            feedsFilteredMsg.setText("{md-info-outline} " + getString(R.string.subscriptions_are_filtered));
            Iconify.addIcons(feedsFilteredMsg);
            feedsFilteredMsg.setVisibility(View.VISIBLE);
        } else {
            feedsFilteredMsg.setVisibility(View.GONE);
        }
    }
    private void showTagBar(boolean show) {
        tagRecycler.setVisibility(show ? View.VISIBLE : View.GONE );
    }
    private int getDefaultNumOfColumns() {
        return getResources().getInteger(R.integer.subscriptions_default_num_of_columns);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Feed feed = subscriptionAdapter.getSelectedFeed();
        if (feed == null) {
            return false;
        }
        int itemId = item.getItemId();
        if (itemId == R.id.remove_all_new_flags_item) {
            displayConfirmationDialog(
                    R.string.remove_all_new_flags_label,
                    R.string.remove_all_new_flags_confirmation_msg,
                    () -> DBWriter.removeFeedNewFlag(feed.getId()));
            return true;
        } else if (itemId == R.id.edit_tags) {
            TagSettingsDialog.newInstance(feed.getPreferences()).show(getChildFragmentManager(), TagSettingsDialog.TAG);
        } else if (itemId == R.id.mark_all_read_item) {
            displayConfirmationDialog(
                    R.string.mark_all_read_label,
                    R.string.mark_all_read_confirmation_msg,
                    () -> DBWriter.markFeedRead(feed.getId()));
            return true;
        } else if (itemId == R.id.rename_item) {
            new RenameFeedDialog(getActivity(), feed).show();
            return true;
        } else if (itemId == R.id.remove_item) {
            RemoveFeedDialog.show(getContext(), feed, null);
            return true;
        } else if (itemId == R.id.multi_select) {
            speedDialView.setVisibility(View.VISIBLE);
            return subscriptionAdapter.onContextItemSelected(item);
        } else if (itemId == R.id.reorder) {
            List<String> entryValues =
                    Arrays.asList(getContext().getResources().getStringArray(R.array.nav_drawer_feed_order_values));
            UserPreferences.setFeedOrder(entryValues.get(UserPreferences.FEED_ORDER_PRIORITY));

//            clearTagFilterIds();
            Pair<List<NavDrawerData.DrawerItem>,
                    List<NavDrawerData.TagDrawerItem>> feedsAndTags =
                    extractFeedsAndTags(listItems);
            tagFilteredFeeds = feedsAndTags.first;
            List<NavDrawerData.TagDrawerItem> tags = feedsAndTags.second;
            initTagViews(tags);

            subscriptionAdapter.setItems(sortFeeds(tagFilteredFeeds));
            //Update subscriptions
            subscriptionAdapter.startPriorityActionMode();
            ItemTouchHelper.Callback callback =
                    new DragAndDropItemTouchHelper(subscriptionAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(subscriptionRecycler);

            subscriptionAdapter.setStartDragListener(viewHolder -> {
                itemTouchHelper.startDrag(viewHolder);
            });
            subscriptionAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setEnabled(false);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private <T> void displayConfirmationDialog(@StringRes int title, @StringRes int message, Callable<? extends T> task) {
        ConfirmationDialog dialog = new ConfirmationDialog(getActivity(), title, message) {
            @Override
            @SuppressLint("CheckResult")
            public void onConfirmButtonPressed(DialogInterface clickedDialog) {
                clickedDialog.dismiss();
                Observable.fromCallable(task)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> loadSubscriptions(),
                                error -> Log.e(TAG, Log.getStackTraceString(error)));
            }
        };
        dialog.createNewDialog().show();
    }

    private void endDragDropMode() {
        subscriptionAdapter.endPriorityActionMode();
//        loadSubscriptions();
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setEnabled(true);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedListChanged(FeedListUpdateEvent event) {
        loadSubscriptions();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadItemsChanged(UnreadItemsUpdateEvent event) {
        loadSubscriptions();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DownloadEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
    }

    @Override
    public void onEndSelectMode() {
        speedDialView.close();
        speedDialView.setVisibility(View.GONE);
        subscriptionAdapter.setItems(tagFilteredFeeds);
        showTagBar(true);
    }

    @Override
    public void onStartSelectMode() {
        showTagBar(false);
        List<NavDrawerData.DrawerItem> feedsOnly = new ArrayList<>();
        for (NavDrawerData.DrawerItem item : tagFilteredFeeds) {
            if (item.type == NavDrawerData.DrawerItem.Type.FEED) {
                feedsOnly.add(item);
            }
        }
        subscriptionAdapter.setItems(feedsOnly);
    }
    public Pair<List<NavDrawerData.DrawerItem>, List<NavDrawerData.TagDrawerItem>>
    extractFeedsAndTags(List<NavDrawerData.DrawerItem> drawerItems) {
        List<NavDrawerData.DrawerItem> feeds = new ArrayList<>();
        List<NavDrawerData.TagDrawerItem> tags = new ArrayList<>();
        for (NavDrawerData.DrawerItem drawerItem : drawerItems) {
            if (drawerItem.type.equals(NavDrawerData.DrawerItem.Type.TAG)) {
                tags.add((NavDrawerData.TagDrawerItem) drawerItem);
                if (((NavDrawerData.TagDrawerItem) drawerItem).name.equals(FeedPreferences.TAG_ROOT)) {
                    rootFolder = (NavDrawerData.TagDrawerItem) drawerItem;
                }
            } else {
                feeds.add(drawerItem);
            }
        }

        List<NavDrawerData.DrawerItem> tagFilteredFeeds = getTagFilteredFeeds(tags);

        Pair<List<NavDrawerData.DrawerItem>, List<NavDrawerData.TagDrawerItem>> feedsAndTags =
                new Pair(tagFilteredFeeds, tags);
        return feedsAndTags;
    }
    private List<NavDrawerData.DrawerItem> getTagFilteredFeeds(List<NavDrawerData.TagDrawerItem> tags) {
       Long tagFilterId = getTagFilterId();

        Set<String> idSet = new HashSet<>();
        if (tagFilterId > 0)
            idSet.add(String.valueOf(tagFilterId));

        TagFilter tagFilter = new TagFilter(idSet);

        List<NavDrawerData.DrawerItem> tagFilteredFeeds = tagFilter.filter(tags);

        return tagFilteredFeeds;
    }
    private void initTagViews(List<NavDrawerData.TagDrawerItem> tags) {
        feedTagAdapter = new FeedTagAdapter(getContext(), new ArrayList<>());
//        Set<String> tagFilterIds = tagFilterId();

        for (NavDrawerData.TagDrawerItem folder : tags) {
            if (!folder.name.equals(FeedPreferences.TAG_ROOT))
                feedTagAdapter.addItem(folder);
        }

        tagRecycler.setAdapter(feedTagAdapter);
    }

    private void updateDisplayedSubscriptions(Long filteredTagId) {
        if (filteredTagId > 0)  {
            Set<NavDrawerData.DrawerItem> allChildren = new HashSet<>();
            for(NavDrawerData.TagDrawerItem tagIter : feedTagAdapter.getFeedFolders()) {
                if (tagIter.id == filteredTagId) {
                    allChildren.addAll(tagIter.children);
                    break;
                }
            }
            tagFilteredFeeds = new ArrayList(allChildren);
        } else {
            tagFilteredFeeds = new ArrayList(rootFolder.children);
        }
        subscriptionAdapter.setItems(sortFeeds(tagFilteredFeeds));
    }

    private List<NavDrawerData.DrawerItem> sortFeeds(List<NavDrawerData.DrawerItem> items) {
        return FeedSorter.sortFeeds(items);
    }

    public Long getTagFilterId() {
        return prefs.getLong(PREF_TAG_FILTER, FeedTagAdapter.ID_ALL);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_TAG_FILTER)) {
            Long filteredTagId = prefs.getLong(key, FeedTagAdapter.ID_ALL);
            updateDisplayedSubscriptions(filteredTagId);
        }
    }
}
