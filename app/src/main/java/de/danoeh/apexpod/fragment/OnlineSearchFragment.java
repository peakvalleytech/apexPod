package de.danoeh.apexpod.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.adapter.discovery.PodcastSearchResultAdapter;
import de.danoeh.apexpod.core.event.FeedListUpdateEvent;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.discovery.PodcastSearchResult;
import de.danoeh.apexpod.discovery.PodcastSearcher;
import de.danoeh.apexpod.discovery.PodcastSearcherRegistry;
import de.danoeh.apexpod.model.feed.Feed;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class OnlineSearchFragment extends Fragment {

    private static final String TAG = "FyydSearchFragment";
    private static final String ARG_SEARCHER = "searcher";
    private static final String ARG_QUERY = "query";

    /**
     * Adapter responsible with the search results
     */
    private PodcastSearchResultAdapter adapter;
    private PodcastSearcher searchProvider;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtvError;
    private Button butRetry;
    private TextView txtvEmpty;

    /**
     * List of podcasts retreived from the search
     */
    private List<PodcastSearchResult> searchResults;
    private List<Feed> subscribedFeeds;
    private Disposable disposable;
    private Disposable updater;

    public static OnlineSearchFragment newInstance(Class<? extends PodcastSearcher> searchProvider) {
        return newInstance(searchProvider, null);
    }

    public static OnlineSearchFragment newInstance(Class<? extends PodcastSearcher> searchProvider, String query) {
        OnlineSearchFragment fragment = new OnlineSearchFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_SEARCHER, searchProvider.getName());
        arguments.putString(ARG_QUERY, query);
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Constructor
     */
    public OnlineSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (PodcastSearcherRegistry.SearcherInfo info : PodcastSearcherRegistry.getSearchProviders()) {
            if (info.searcher.getClass().getName().equals(getArguments().getString(ARG_SEARCHER))) {
                searchProvider = info.searcher;
                break;
            }
        }
        if (searchProvider == null) {
            throw new IllegalArgumentException("Podcast searcher not found");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_itunes_search, container, false);
        root.findViewById(R.id.spinner_country).setVisibility(INVISIBLE);
        recyclerView = root.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        progressBar = root.findViewById(R.id.progressBar);
        txtvError = root.findViewById(R.id.txtvError);
        butRetry = root.findViewById(R.id.butRetry);
        txtvEmpty = root.findViewById(android.R.id.empty);
        TextView txtvPoweredBy = root.findViewById(R.id.search_powered_by);
        txtvPoweredBy.setText(getString(R.string.search_powered_by, searchProvider.getName()));
        setupToolbar(root.findViewById(R.id.toolbar));

//        recyclerView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
//                    InputMethodManager imm = (InputMethodManager)
//                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            }
//        });
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }

        if (updater != null) {
            updater.dispose();
        }

        adapter = null;
    }
    @Subscribe
    public void onFeedListChanged(FeedListUpdateEvent event) {
        loadData(feeds -> {
            subscribedFeeds = feeds;
            if (adapter != null)
                adapter.updateSubscribedList(subscribedFeeds);
        });
    }

    private void setupToolbar(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.online_search);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        final SearchView sv = (SearchView) searchItem.getActionView();
        sv.setQueryHint(getString(R.string.search_podcast_hint));
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                sv.clearFocus();
                if (subscribedFeeds == null) {
                    loadData(feeds -> {
                        subscribedFeeds = feeds;
                        search(s);
                    });
                } else {
                    search(s);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        sv.setOnQueryTextFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                showInputMethod(view.findFocus());
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            }
        });
        searchItem.expandActionView();

        if (getArguments().getString(ARG_QUERY, null) != null) {
            sv.setQuery(getArguments().getString(ARG_QUERY, null), true);
        }
    }

    private void loadData(Consumer<List<Feed>> onNext) {
        disposable = Observable.fromCallable(DBReader::getFeedList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    onNext,
                    error -> {
                        Log.e(TAG, error.getMessage());
                    });
    }

    private void search(String query) {
        if (disposable != null) {
            disposable.dispose();
        }
        showOnlyProgressBar();
        disposable = searchProvider.search(query).subscribe(result -> {
            searchResults = result;
            progressBar.setVisibility(View.GONE);
            adapter = new PodcastSearchResultAdapter(getActivity(), searchResults, new ArrayList<>());
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(!searchResults.isEmpty() ? View.VISIBLE : View.GONE);
            txtvEmpty.setVisibility(searchResults.isEmpty() ? View.VISIBLE : View.GONE);
            adapter.updateSubscribedList(subscribedFeeds);
            recyclerView.notify();
            txtvEmpty.setText(getString(R.string.no_results_for_query, query));
        }, error -> {
                Log.e(TAG, Log.getStackTraceString(error));
                progressBar.setVisibility(View.GONE);
                txtvError.setText(error.toString());
                txtvError.setVisibility(View.VISIBLE);
                butRetry.setOnClickListener(v -> search(query));
                butRetry.setVisibility(View.VISIBLE);
            });
    }

    private void showOnlyProgressBar() {
        recyclerView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }
}
