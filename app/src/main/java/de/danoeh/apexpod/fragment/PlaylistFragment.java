package de.danoeh.apexpod.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.activity.MainActivity;
import de.danoeh.apexpod.adapter.PlayListsListAdapter;
import de.danoeh.apexpod.core.storage.repository.PlaylistRepository;
import de.danoeh.apexpod.core.storage.repository.impl.PlaylistRepositoryImpl;
import de.danoeh.apexpod.model.Playlist;
import de.danoeh.apexpod.view.EmptyViewHandler;
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
        MainActivity activity = ((MainActivity) getActivity());
        activity.setupToolbarToggle(toolbar, displayUpArrow);
        refreshToolbarState();

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setRecycledViewPool(((MainActivity) getActivity()).getRecycledViewPool());
        adapter = new PlayListsListAdapter(playlists);
        adapter.setOnItemClickListener(new PlayListsListAdapter.OnItemClickedListener() {
            @Override
            public void onItemClicked(Playlist playlist) {
                Bundle args = new Bundle();
                args.putSerializable(PlayListItemFragment.ARG_PLAYLIST, playlist);
                activity.loadFragment(PlayListItemFragment.TAG, args);
            }
        });
        recyclerView.setAdapter(adapter);
        progressBar = root.findViewById(R.id.progLoading);

        emptyView = new EmptyViewHandler(getActivity());
        emptyView.setIcon(R.drawable.ic_playlist);
        emptyView.setTitle(R.string.no_history_head_label);
        emptyView.setMessage(R.string.no_history_label);
        emptyView.attachToRecyclerView(recyclerView);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadItems();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_UP_ARROW, displayUpArrow);
        super.onSaveInstanceState(outState);
    }

    public void refreshToolbarState() {
        boolean hasHistory = playlists != null && !playlists.isEmpty();
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

        playlistRepository =  new PlaylistRepositoryImpl(getContext());
        List<Playlist> playlists = playlistRepository.getPlaylists();
        return playlists;
    }
}
