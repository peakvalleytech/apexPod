package de.danoeh.apexpod.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.activity.OnlineFeedViewActivity;
import de.danoeh.apexpod.adapter.FeedDiscoverAdapter;
import de.danoeh.apexpod.core.event.DiscoveryDefaultUpdateEvent;
import de.danoeh.apexpod.discovery.ItunesTopListLoader;
import de.danoeh.apexpod.discovery.PodcastSearchResult;
import io.reactivex.disposables.Disposable;


public class MainFragment extends Fragment implements Toolbar.OnMenuItemClickListener {
    public static final String TAG = "MainFragment";
    private static final int NUM_SUGGESTIONS = 12;
    private static final String KEY_UP_ARROW = "up_arrow";

    private ProgressBar progressBar;
    private Disposable disposable;
    private TextView errorTextView;
    private LinearLayout errorView;
    private Toolbar toolbar;
    private boolean displayUpArrow;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.main_fragment, container, false);
        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(this);
        displayUpArrow = getParentFragmentManager().getBackStackEntryCount() != 0;
        if (savedInstanceState != null) {
            displayUpArrow = savedInstanceState.getBoolean(KEY_UP_ARROW);
        }
        ((MainActivity) getActivity()).setupToolbarToggle(toolbar, displayUpArrow);
        toolbar.inflateMenu(R.menu.queue);


        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void loadToplist() {
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
