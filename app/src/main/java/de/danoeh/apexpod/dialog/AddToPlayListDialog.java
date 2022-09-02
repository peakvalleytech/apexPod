package de.danoeh.apexpod.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.storage.ApexDBAdapter;
import de.danoeh.apexpod.core.storage.database.PlayListItemDao;
import de.danoeh.apexpod.databinding.EditPlaylistsDialogBinding;
import de.danoeh.apexpod.model.Playlist;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.view.ItemOffsetDecoration;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddToPlayListDialog extends DialogFragment {
    public static final String TAG = "AddToPlayListDialog";
    private static final String ARG_FEEDITEM = "feeditem";
    private Set<Playlist> selectedPlaylists = new HashSet<>();
    private List<Playlist> allPlaylists = new ArrayList<>();
    private EditPlaylistsDialogBinding viewBinding;
    private PlaystListSelectionAdapter adapter;
    private ApexDBAdapter dbAdapter;
    private PlayListItemDao playListItemDao;
    FeedItem feedItem;
    public static AddToPlayListDialog newInstance(FeedItem feedItemId) {
        AddToPlayListDialog fragment = new AddToPlayListDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FEEDITEM, feedItemId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dbAdapter = ApexDBAdapter.getInstance();
        playListItemDao = new PlayListItemDao();
        feedItem = (FeedItem) getArguments().getSerializable(ARG_FEEDITEM);

        selectedPlaylists = new HashSet<>(dbAdapter.getPlaylListsByFeedId(feedItem.getId()));

        viewBinding = EditPlaylistsDialogBinding.inflate(getLayoutInflater());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        viewBinding.playlistsRecycler.setLayoutManager(layoutManager);
        viewBinding.playlistsRecycler.setHasFixedSize(true);
        viewBinding.playlistsRecycler.addItemDecoration(new ItemOffsetDecoration(getContext(), 4));
        adapter = new PlaystListSelectionAdapter();

        adapter.setHasStableIds(true);
        viewBinding.playlistsRecycler.setAdapter(adapter);

        viewBinding.newPlaylistButton.setOnClickListener(v -> {
                    String playListName = viewBinding.newPlaylistEditText.getText().toString().trim();
                    createPlaylist(playListName);
                });

        loadPlaylists();

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setView(viewBinding.getRoot());
        dialog.setTitle(R.string.playlists_label);
        dialog.setPositiveButton(android.R.string.ok, (d, input) -> {
            String playListName = viewBinding.newPlaylistEditText.getText().toString().trim();
            createPlaylist(playListName);
        });
        dialog.setNegativeButton(R.string.cancel_label, null);
        return dialog.create();
    }

    private void loadPlaylists() {
        Observable.fromCallable(
                () -> {
                    List<Playlist> playlists = dbAdapter.getAllPlaylist();
                    ArrayList<String> playListTitles = new ArrayList<String>();
                    for (Playlist p : playlists) {
                        playListTitles.add(p.getName());
                    }

                    allPlaylists = playlists;
                    return playListTitles;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                        }, error -> {
                            Log.e(TAG, Log.getStackTraceString(error));
                        });
    }

    private boolean isValidInput(String playlistName) {
        if (TextUtils.isEmpty(playlistName)) {
            return false;
        }
        for (Playlist playlist : selectedPlaylists) {
            if (playlist.getName().equals(playlistName)) {
                return false;
            }
        }
        return true;
    }

    private void selectPlaylist(Playlist playlist) {
        selectedPlaylists.add(playlist);
        ArrayList<FeedItem> feedItems = new ArrayList<>();
        feedItems.add(feedItem);
        playListItemDao.addItemsByPlayistId(playlist.getId(), feedItems);
        adapter.notifyDataSetChanged();
    }

    private void createPlaylist(String playListName) {
        if (!isValidInput(playListName)) {
            return;
        }
        Playlist playList = null;
        viewBinding.newPlaylistEditText.setText("");
        playList = new Playlist(playListName);
        long id = dbAdapter.createPlaylist(playList);
        playList.setId(id);
        allPlaylists.add(playList);
        selectPlaylist(playList);
        adapter.notifyDataSetChanged();
        viewBinding.playlistsRecycler.scrollToPosition(allPlaylists.size() - 1);
    }

    public class PlaystListSelectionAdapter extends RecyclerView.Adapter<PlaystListSelectionAdapter.ViewHolder> {
        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CheckBox checkBox = new CheckBox(getContext());
            return new ViewHolder(checkBox);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Playlist playlist = allPlaylists.get(holder.getBindingAdapterPosition());
            holder.checkBox.setChecked(selectedPlaylists.contains(playlist));
            holder.checkBox.setText(playlist.getName());
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectPlaylist(playlist);
                    selectedPlaylists.add(playlist);
                } else {
                    selectedPlaylists.remove(playlist);
                    playListItemDao.deleteItemByPlayListId(playlist.getId(), feedItem);
                }

            });
        }

        @Override
        public int getItemCount() {
            return allPlaylists.size();
        }

        @Override
        public long getItemId(int position) {
            return allPlaylists.get(position).hashCode();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;

            ViewHolder(CheckBox itemView) {
                super(itemView);
                checkBox = itemView;
            }
        }
    }
}
