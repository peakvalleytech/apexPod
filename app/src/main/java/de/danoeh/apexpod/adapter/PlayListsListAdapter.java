package de.danoeh.apexpod.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.model.Playlist;

public class PlayListsListAdapter extends RecyclerView.Adapter<PlayListsListAdapter.PlayListsListViewHolder> {
    public List<Playlist> playlists;
    public interface OnItemClickedListener {
        void onItemClicked(Playlist playlist);
    }
    OnItemClickedListener onItemClickListener;
    public PlayListsListAdapter(List<Playlist> playlists) {
        super();
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public PlayListsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_playlist, parent, false);
        return new PlayListsListViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListsListViewHolder holder, int position) {
        holder.nameTextView.setText(playlists.get(position).getName());
        holder.itemView.setOnClickListener(v -> {
            onItemClickListener.onItemClicked(playlists.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    class PlayListsListViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        public PlayListsListViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
        }
    }

    public void setOnItemClickListener(OnItemClickedListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}