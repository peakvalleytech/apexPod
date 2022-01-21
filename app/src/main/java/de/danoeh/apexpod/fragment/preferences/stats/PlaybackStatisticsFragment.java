package de.danoeh.apexpod.fragment.preferences.stats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.adapter.PlaybackStatisticsListAdapter;
import de.danoeh.apexpod.core.dialog.ConfirmationDialog;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.StatisticsItem;
import de.danoeh.apexpod.core.util.comparator.CompareCompat;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.Collections;
import java.util.List;

/**
 * Displays the 'playback statistics' screen
 */
public class PlaybackStatisticsFragment extends Fragment {
    private static final String TAG = PlaybackStatisticsFragment.class.getSimpleName();
    private static final String PREF_NAME = "StatisticsActivityPrefs";
    private static final String PREF_COUNT_ALL = "countAll";

    private Disposable disposable;
    private RecyclerView feedStatisticsList;
    private ProgressBar progressBar;
    private PlaybackStatisticsListAdapter listAdapter;
    private boolean countAll = false;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        countAll = prefs.getBoolean(PREF_COUNT_ALL, false);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.statistics_activity, container, false);
        feedStatisticsList = root.findViewById(R.id.statistics_list);
        progressBar = root.findViewById(R.id.progressBar);
        listAdapter = new PlaybackStatisticsListAdapter(getContext());
        listAdapter.setCountAll(countAll);
        feedStatisticsList.setLayoutManager(new LinearLayoutManager(getContext()));
        feedStatisticsList.setAdapter(listAdapter);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshStatistics();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.statistics, menu);
        menu.findItem(R.id.statistics_reset).setEnabled(!countAll);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.statistics_reset) {
            confirmResetStatistics();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmResetStatistics() {
        if (!countAll) {
            ConfirmationDialog conDialog = new ConfirmationDialog(
                    getActivity(),
                    R.string.statistics_reset_data,
                    R.string.statistics_reset_data_msg) {

                @Override
                public void onConfirmButtonPressed(DialogInterface dialog) {
                    dialog.dismiss();
                    doResetStatistics();
                }
            };
            conDialog.createNewDialog().show();
        }
    }

    private void doResetStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        feedStatisticsList.setVisibility(View.GONE);
        if (disposable != null) {
            disposable.dispose();
        }

        disposable = Completable.fromFuture(DBWriter.resetStatistics())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    refreshStatistics();
                    UserPreferences.resetUsageCountingDate();
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private void refreshStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        feedStatisticsList.setVisibility(View.GONE);
        loadStatistics();
    }

    private void loadStatistics() {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Observable.fromCallable(this::fetchStatistics)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    listAdapter.update(result);
                    progressBar.setVisibility(View.GONE);
                    feedStatisticsList.setVisibility(View.VISIBLE);
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private List<StatisticsItem> fetchStatistics() {
        List<StatisticsItem> statisticsData = DBReader.getStatistics();
        if (countAll) {
            Collections.sort(statisticsData, (item1, item2) ->
                    CompareCompat.compareLong(item1.timePlayedCountAll, item2.timePlayedCountAll));
        } else {
            Collections.sort(statisticsData, (item1, item2) ->
                    CompareCompat.compareLong(item1.timePlayed, item2.timePlayed));
        }
        return statisticsData;
    }
}
