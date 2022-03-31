package de.danoeh.apexpod.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.adapter.discovery.PodcastSearchResultAdapter;
import de.danoeh.apexpod.core.event.DiscoveryDefaultUpdateEvent;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.discovery.ItunesTopListLoader;
import de.danoeh.apexpod.discovery.PodcastSearchResult;
import de.danoeh.apexpod.model.feed.Feed;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Searches iTunes store for top podcasts and displays results in a list.
 */
public class DiscoveryFragment extends Fragment {

    private static final String TAG = "ItunesSearchFragment";
    private SharedPreferences prefs;

    /**
     * Adapter responsible with the search results.
     */
    private PodcastSearchResultAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtvError;
    private Button butRetry;
    private TextView txtvEmpty;

    /**
     * List of podcasts retreived from the search.
     */
    private List<PodcastSearchResult> searchResults;
    private List<PodcastSearchResult> topList;
    private Disposable disposable;
    private String countryCode = "US";
    private List<Feed> subscribedFeeds;
    /**
     * Replace adapter data with provided search results from SearchTask.
     * @param result List of Podcast objects containing search results
     */
    private void updateData(List<PodcastSearchResult> result) {
        this.searchResults = result;
        if (result != null && result.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            txtvEmpty.setVisibility(View.GONE);
            adapter = new PodcastSearchResultAdapter((MainActivity) getActivity(), getContext(), result, subscribedFeeds);
            recyclerView.setAdapter(adapter);
        } else {
            recyclerView.setVisibility(View.GONE);
            txtvEmpty.setVisibility(View.VISIBLE);
        }
    }

    public DiscoveryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getActivity().getSharedPreferences(ItunesTopListLoader.PREFS, MODE_PRIVATE);
        countryCode = prefs.getString(ItunesTopListLoader.PREF_KEY_COUNTRY_CODE, Locale.getDefault().getCountry());
        loadData();
    }

    private void loadData() {
        disposable = Observable.fromCallable(DBReader::getFeedList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(feeds -> {
                    subscribedFeeds = feeds;
                    loadToplist(countryCode);
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_itunes_search, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new PodcastSearchResultAdapter((MainActivity) getActivity(), getContext(), new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        //Show information about the podcast when the list item is clicked
//        recyclerView.setOnItemClickListener((parent, view1, position, id) -> {
//            PodcastSearchResult podcast = searchResults.get(position);
//            if (podcast.feedUrl == null) {
//                return;
//            }
//            Intent intent = new Intent(getActivity(), OnlineFeedViewActivity.class);
//            intent.putExtra(OnlineFeedViewActivity.ARG_FEEDURL, podcast.feedUrl);
//            startActivity(intent);
//        });


        List<String> countryCodeArray = new ArrayList<String>(Arrays.asList(Locale.getISOCountries()));
        HashMap<String, String> countryCodeNames = new HashMap<String, String>();
        for (String code: countryCodeArray) {
            Locale locale = new Locale("", code);
            String countryName = locale.getDisplayCountry();
            if (countryName != null) {
                countryCodeNames.put(code, countryName);
            }
        }

        List<String> countryNamesSort = new ArrayList<String>(countryCodeNames.values());
        Collections.sort(countryNamesSort);
        countryNamesSort.add(0, getResources().getString(R.string.discover_hide));

        Spinner countrySpinner = root.findViewById(R.id.spinner_country);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getContext(), 
                android.R.layout.simple_spinner_item, 
                countryNamesSort);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(dataAdapter);
        int pos = countryNamesSort.indexOf(countryCodeNames.get(countryCode));
        countrySpinner.setSelection(pos);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> countrySpinner, View view, int position, long id) {
                String countryName = (String) countrySpinner.getItemAtPosition(position);

                if (countryName.equals(getResources().getString(R.string.discover_hide))) {
                    countryCode = ItunesTopListLoader.DISCOVER_HIDE_FAKE_COUNTRY_CODE;
                } else {
                    for (Object o : countryCodeNames.keySet()) {
                        if (countryCodeNames.get(o).equals(countryName)) {
                            countryCode = o.toString();
                            break;
                        }
                    }
                }

                prefs.edit()
                        .putString(ItunesTopListLoader.PREF_KEY_COUNTRY_CODE, countryCode)
                        .apply();

                EventBus.getDefault().post(new DiscoveryDefaultUpdateEvent());
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        progressBar = root.findViewById(R.id.progressBar);
        txtvError = root.findViewById(R.id.txtvError);
        butRetry = root.findViewById(R.id.butRetry);
        txtvEmpty = root.findViewById(android.R.id.empty);

        loadData();
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
    }

    private void loadToplist(String country) {
        if (disposable != null) {
            disposable.dispose();
        }

        recyclerView.setVisibility(View.GONE);
        txtvError.setVisibility(View.GONE);
        butRetry.setVisibility(View.GONE);
        txtvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (country.equals(ItunesTopListLoader.DISCOVER_HIDE_FAKE_COUNTRY_CODE)) {
            recyclerView.setVisibility(View.GONE);
            txtvError.setVisibility(View.VISIBLE);
            txtvError.setText(getResources().getString(R.string.discover_is_hidden));
            butRetry.setVisibility(View.GONE);
            txtvEmpty.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            ItunesTopListLoader loader = new ItunesTopListLoader(getContext());
            disposable = loader.loadToplist(country, 25).subscribe(
                    podcasts -> {
                        progressBar.setVisibility(View.GONE);
                        topList = podcasts;
                        updateData(topList);
                    }, error -> {
                        Log.e(TAG, Log.getStackTraceString(error));
                        progressBar.setVisibility(View.GONE);
                        txtvError.setText(error.getMessage());
                        txtvError.setVisibility(View.VISIBLE);
                        butRetry.setOnClickListener(v -> loadData());
                        butRetry.setVisibility(View.VISIBLE);
                    });
        }
    }
}
