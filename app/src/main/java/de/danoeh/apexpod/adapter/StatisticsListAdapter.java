package de.danoeh.apexpod.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.glide.ApGlideSettings;
import de.danoeh.apexpod.core.storage.StatisticsItem;
import de.danoeh.apexpod.model.stats.FeedPlayStats;
import de.danoeh.apexpod.model.stats.FeedPlayStatsItem;
import de.danoeh.apexpod.view.PieChartView;

import java.util.List;

/**
 * Parent Adapter for the playback and download statistics list.
 */
public abstract class StatisticsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FEED = 1;
    final Context context;
    private FeedPlayStats statisticsData;
    PieChartView.PieChartData pieChartData;

    StatisticsListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return statisticsData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_FEED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.statistics_listitem_total, parent, false);
            TextView totalText = view.findViewById(R.id.total_description);
            totalText.setText(getHeaderCaption());
            TextView subheaderLabel = view.findViewById(R.id.total_speed_adjusted_time_label);
            String subHeaderCaption = getSubheaderCaption();
            if (subHeaderCaption.isEmpty()) {
                subheaderLabel.setVisibility(View.GONE);
            }
            subheaderLabel.setText(getSubheaderCaption());
            return new HeaderHolder(view);
        }
        return new StatisticsHolder(inflater.inflate(R.layout.statistics_listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            HeaderHolder holder = (HeaderHolder) h;
            holder.pieChart.setData(pieChartData);
            holder.totalTime.setText(getHeaderValue());
            if (getSubheaderValue().isEmpty()) {
                holder.speedAdjustedTimeValue.setVisibility(View.GONE);
            }
            holder.speedAdjustedTimeValue.setText(getSubheaderValue());
        } else {
            StatisticsHolder holder = (StatisticsHolder) h;
            FeedPlayStatsItem statsItem = statisticsData.getItems().get(position - 1);

            Glide.with(context)
                    .load(statsItem.getFeed().getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.light_gray)
                            .error(R.color.light_gray)
                            .diskCacheStrategy(ApGlideSettings.AP_DISK_CACHE_STRATEGY)
                            .fitCenter()
                            .dontAnimate())
                    .into(holder.image);

            holder.title.setText(statsItem.getFeed().getTitle());
            holder.chip.setTextColor(pieChartData.getColorOfItem(position - 1));
            onBindFeedViewHolder(holder, statsItem);
        }
    }


    public void update(FeedPlayStats feedPlayStats) {
        statisticsData = feedPlayStats;
        pieChartData = generateChartData(feedPlayStats);
        notifyDataSetChanged();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView totalTime;
        PieChartView pieChart;
        TextView speedAdjustedTimeValue;

        HeaderHolder(View itemView) {
            super(itemView);
            totalTime = itemView.findViewById(R.id.total_time);
            pieChart = itemView.findViewById(R.id.pie_chart);
            speedAdjustedTimeValue = itemView.findViewById(R.id.total_speed_adjusted_time_value);
        }
    }

    static class StatisticsHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView value;
        TextView chip;

        StatisticsHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgvCover);
            title = itemView.findViewById(R.id.txtvTitle);
            value = itemView.findViewById(R.id.txtvValue);
            chip = itemView.findViewById(R.id.chip);
        }
    }

    abstract String getHeaderCaption();

    abstract String getHeaderValue();

    abstract String getSubheaderCaption();

    abstract String getSubheaderValue();

    abstract PieChartView.PieChartData generateChartData(FeedPlayStats statisticsData);

    abstract void onBindFeedViewHolder(StatisticsHolder holder, FeedPlayStatsItem item);
}
