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
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.core.storage.NavDrawerData;
import de.danoeh.apexpod.databinding.EditPlaylistsDialogBinding;
import de.danoeh.apexpod.databinding.EditTagsDialogBinding;
import de.danoeh.apexpod.model.Playlist;
import de.danoeh.apexpod.model.feed.FeedItem;
import de.danoeh.apexpod.model.feed.FeedPreferences;
import de.danoeh.apexpod.view.ItemOffsetDecoration;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddToPlayListDialog extends DialogFragment {
    public static final String TAG = "TagSettingsDialog";
    private static final String ARG_FEED_PREFERENCES = "feed_preferences";
    private List<Playlist> displayedPlayLists;
    private EditPlaylistsDialogBinding viewBinding;
    private PlaystListSelectionAdapter adapter;

    public static AddToPlayListDialog newInstance(long feedItemId) {
        AddToPlayListDialog fragment = new AddToPlayListDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FEED_PREFERENCES, preferences);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FeedPreferences preferences = (FeedPreferences) getArguments().getSerializable(ARG_FEED_PREFERENCES);
//        displayedPlayLists = new ArrayList<Playlist>(preferences.getTags());
        displayedPlayLists.remove(FeedPreferences.TAG_ROOT);

        viewBinding = EditPlaylistsDialogBinding.inflate(getLayoutInflater());
        viewBinding.playlistsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        viewBinding.playlistsRecycler.addItemDecoration(new ItemOffsetDecoration(getContext(), 4));
        adapter = new PlaystListSelectionAdapter();
        adapter.setHasStableIds(true);
        viewBinding.playlistsRecycler.setAdapter(adapter);

        viewBinding.newPlaylistButton.setOnClickListener(v -> {
                    String playListName = viewBinding.newPlaylistEditText.getText().toString().trim();
                    isValidInput(playListName);
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
        dialog.setTitle(R.string.feed_tags_label);
        dialog.setPositiveButton(android.R.string.ok, (d, input) -> {
            String playListName = viewBinding.newPlaylistEditText.getText().toString().trim();
            isValidInput(playListName);
            addPlayList(playListName);
            preferences.getTags().clear();
            preferences.getTags().addAll(displayedPlayLists);
            DBWriter.setFeedPreferences(preferences);
        });
        dialog.setNegativeButton(R.string.cancel_label, null);
        return dialog.create();
    }

    private void loadPlaylists() {
        Observable.fromCallable(
                () -> {
                    List<Playlist> playlists = ApexDBAdapter.getInstance().getAllPlaylist();
                    ArrayList<String> playListTitles = new ArrayList<String>();
                    for (Playlist p : playlists) {
                        playListTitles.add(p.getName());
                    }
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
        Playlist playList = new Playlist(playListName);
        displayedPlayLists.add(playList);
        viewBinding.newPlaylistEditText.setText("");
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
                displayedPlayLists.remove(position);
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
