package de.danoeh.apexpod.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

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
    private List<Playlist> displayedPlayLists = new ArrayList<>();
    private List<Playlist> createdPlayLists = new ArrayList<>();
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

        displayedPlayLists = dbAdapter.getPlaylListsByFeedId(feedItem.getId());

        viewBinding = EditPlaylistsDialogBinding.inflate(getLayoutInflater());
        viewBinding.playlistsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        viewBinding.playlistsRecycler.addItemDecoration(new ItemOffsetDecoration(getContext(), 4));
        adapter = new PlaystListSelectionAdapter();
        adapter.setHasStableIds(true);
        viewBinding.playlistsRecycler.setAdapter(adapter);

        viewBinding.newPlaylistButton.setOnClickListener(v -> {
                    String playListName = viewBinding.newPlaylistEditText.getText().toString().trim();
                    addPlayList(playListName);
                });

        loadPlaylists();
        viewBinding.newPlaylistEditText.setThreshold(1);
        viewBinding.newPlaylistEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewBinding.newPlaylistEditText.showDropDown();
                viewBinding.newPlaylistEditText.requestFocus();
                return false;
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setView(viewBinding.getRoot());
        dialog.setTitle(R.string.playlists_label);
        dialog.setPositiveButton(android.R.string.ok, (d, input) -> {
            String playListName = viewBinding.newPlaylistEditText.getText().toString().trim();
            addPlayList(playListName);
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

                    createdPlayLists = playlists;
                    return playListTitles;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            ArrayAdapter<String> acAdapter = new ArrayAdapter<String>(getContext(),
                                    R.layout.single_tag_text_view, result);
                            viewBinding.newPlaylistEditText.setAdapter(acAdapter);
                        }, error -> {
                            Log.e(TAG, Log.getStackTraceString(error));
                        });
    }

    private boolean isValidInput(String playlistName) {
        if (TextUtils.isEmpty(playlistName)) {
            return false;
        }
        for (Playlist playlist : displayedPlayLists) {
            if (playlist.getName().equals(playlistName)) {
                return false;
            }
        }
        return true;
    }

    private void addPlayList(String playListName) {
        if (!isValidInput(playListName)) {
            return;
        }
        Playlist playList = null;
        viewBinding.newPlaylistEditText.setText("");
        boolean playlistExists = false;
        for (Playlist pIter : createdPlayLists) {
            if (pIter.getName().equals(playListName)) {
                playlistExists = true;
                playList = pIter;
            }
        }
        if (!playlistExists) {
            playList = new Playlist(playListName);
            long id = dbAdapter.createPlaylist(playList);
            playList.setId(id);
        }
        displayedPlayLists.add(playList);
        ArrayList<FeedItem> feedItems = new ArrayList<>();
        feedItems.add(feedItem);
        playListItemDao.addItemsByPlayistId(playList.getId(), feedItems);
        adapter.notifyDataSetChanged();
    }

    public class PlaystListSelectionAdapter extends RecyclerView.Adapter<PlaystListSelectionAdapter.ViewHolder> {
        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Chip chip = new Chip(getContext());
            chip.setCloseIconVisible(true);
            chip.setCloseIconResource(R.drawable.ic_delete);
            return new ViewHolder(chip);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.chip.setText(displayedPlayLists.get(position).getName());
            holder.chip.setOnCloseIconClickListener(v -> {
                Playlist playlist = displayedPlayLists.get(position);
                displayedPlayLists.remove(position);
                playListItemDao.deleteItemByPlayListId(playlist.getId(), feedItem);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return displayedPlayLists.size();
        }

        @Override
        public long getItemId(int position) {
            return displayedPlayLists.get(position).hashCode();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            Chip chip;

            ViewHolder(Chip itemView) {
                super(itemView);
                chip = itemView;
            }
        }
    }
}
