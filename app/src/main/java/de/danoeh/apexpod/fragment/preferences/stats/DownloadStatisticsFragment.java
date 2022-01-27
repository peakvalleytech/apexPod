package de.danoeh.apexpod.fragment.preferences.stats;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.adapter.DownloadStatisticsListAdapter;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.StatisticsItem;
import de.danoeh.apexpod.core.storage.database.FeedPlayStatsDao;
import de.danoeh.apexpod.core.util.comparator.CompareCompat;
import de.danoeh.apexpod.model.stats.FeedPlayStats;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.Collections;
import java.util.List;

/**
 * Displays the 'download statistics' screen
 */
public class DownloadStatisticsFragment extends Fragment {
    private static final String TAG = DownloadStatisticsFragment.class.getSimpleName();

    private Disposable disposable;
    private RecyclerView downloadStatisticsList;
    private ProgressBar progressBar;
    private DownloadStatisticsListAdapter listAdapter;
    private FeedPlayStatsDao feedPlayStatsDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.statistics_activity, container, false);
        downloadStatisticsList = root.findViewById(R.id.statistics_list);
        progressBar = root.findViewById(R.id.progressBar);
        listAdapter = new DownloadStatisticsListAdapter(getContext());
        downloadStatisticsList.setLayoutManager(new LinearLayoutManager(getContext()));
        downloadStatisticsList.setAdapter(listAdapter);
        feedPlayStatsDao = new FeedPlayStatsDao();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshDownloadStatistics();
    }

    private void refreshDownloadStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        downloadStatisticsList.setVisibility(View.GONE);
        loadStatistics();
    }

    private void loadStatistics() {
        if (disposable != null) {
            disposable.dispose();
        }

        disposable =
                Observable.fromCallable(() -> {
                    FeedPlayStats feedPlayStats = feedPlayStatsDao.getFeedPlayStats();
                    Collections.sort(feedPlayStats.getItems(), (item1, item2) ->
                            CompareCompat.compareLong(item1.getTotalDownloadSize(), item2.getTotalDownloadSize()));
                    return feedPlayStats;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    listAdapter.update(result);
                    progressBar.setVisibility(View.GONE);
                    downloadStatisticsList.setVisibility(View.VISIBLE);
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }
}
