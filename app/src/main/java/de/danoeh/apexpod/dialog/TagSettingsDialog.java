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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.storage.DBReader;
import de.danoeh.apexpod.core.storage.NavDrawerData;
import de.danoeh.apexpod.model.feed.FeedPreferences;
import de.danoeh.apexpod.core.storage.DBWriter;
import de.danoeh.apexpod.databinding.EditTagsDialogBinding;
import de.danoeh.apexpod.view.ItemOffsetDecoration;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class TagSettingsDialog extends DialogFragment {
    public static final String TAG = "TagSettingsDialog";
    private static final String ARG_FEED_PREFERENCES = "feed_preferences";
    private List<String> selectedTags;
    private List<String> allTags = new ArrayList<>();
    private EditTagsDialogBinding viewBinding;
    private TagSelectionAdapter adapter;

    public static TagSettingsDialog newInstance(FeedPreferences preferences) {
        TagSettingsDialog fragment = new TagSettingsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FEED_PREFERENCES, preferences);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FeedPreferences preferences = (FeedPreferences) getArguments().getSerializable(ARG_FEED_PREFERENCES);
        selectedTags = new ArrayList<>(preferences.getTags());
        selectedTags.remove(FeedPreferences.TAG_ROOT);

        viewBinding = EditTagsDialogBinding.inflate(getLayoutInflater());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        viewBinding.tagsRecycler.setLayoutManager(layoutManager);
        viewBinding.tagsRecycler.addItemDecoration(new ItemOffsetDecoration(getContext(), 4));
        adapter = new TagSelectionAdapter();
        adapter.setHasStableIds(true);
        viewBinding.tagsRecycler.setAdapter(adapter);

        viewBinding.newTagButton.setOnClickListener(v ->
                addTag(viewBinding.newTagEditText.getText().toString().trim()));

        loadTags();
        viewBinding.newTagEditText.setThreshold(1);
        viewBinding.newTagEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                viewBinding.newTagEditText.showDropDown();
                viewBinding.newTagEditText.requestFocus();
                return false;
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setView(viewBinding.getRoot());
        dialog.setTitle(R.string.feed_tags_label);
        dialog.setPositiveButton(android.R.string.ok, (d, input) -> {
            addTag(viewBinding.newTagEditText.getText().toString().trim());
            preferences.getTags().clear();
            preferences.getTags().add(FeedPreferences.TAG_ROOT);
            preferences.getTags().addAll(selectedTags);
            DBWriter.setFeedPreferences(preferences);
        });
        dialog.setNegativeButton(R.string.cancel_label, null);
        return dialog.create();
    }

    private void loadTags() {
        Observable.fromCallable(
                () -> {
                    NavDrawerData data = DBReader.getNavDrawerData();
                    List<NavDrawerData.DrawerItem> items = data.items;
                    List<String> tags = new ArrayList<String>();
                    for (NavDrawerData.DrawerItem item : items) {
                        if (item.type == NavDrawerData.DrawerItem.Type.TAG) {
                            NavDrawerData.TagDrawerItem tag = (NavDrawerData.TagDrawerItem) item;
                            if (!tag.name.equals((FeedPreferences.TAG_ROOT))) {
                                tags.add(item.getTitle());
                            }
                        }
                    }
                    return tags;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {

                            allTags = new ArrayList<>(result);
                            adapter.notifyDataSetChanged();
                        }, error -> {
                            Log.e(TAG, Log.getStackTraceString(error));
                        });
    }

    private void addTag(String name) {
        if (TextUtils.isEmpty(name) || selectedTags.contains(name)) {
            return;
        }
        selectedTags.add(name);
        viewBinding.newTagEditText.setText("");
        adapter.notifyDataSetChanged();
    }

    public class TagSelectionAdapter extends RecyclerView.Adapter<TagSelectionAdapter.ViewHolder> {
        @Override
        @NonNull
        public TagSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CheckBox checkbox = new CheckBox(getContext());
            return new TagSelectionAdapter.ViewHolder(checkbox);
        }

        @Override
        public void onBindViewHolder(@NonNull TagSelectionAdapter.ViewHolder holder, int position) {
            String tag = allTags.get(holder.getBindingAdapterPosition());
            holder.checkbox.setChecked(selectedTags.contains(tag));
            holder.checkbox.setText(tag);
            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedTags.add(allTags.get(holder.getBindingAdapterPosition()));
                } else {
                    selectedTags.remove(allTags.get(holder.getBindingAdapterPosition()));
                }
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return allTags.size();
        }

        @Override
        public long getItemId(int position) {
            return allTags.get(position).hashCode();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkbox;

            ViewHolder(CheckBox itemView) {
                super(itemView);
                checkbox = itemView;
            }
        }
    }
}
