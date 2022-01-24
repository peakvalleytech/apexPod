package de.danoeh.apexpod.adapter;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.preferences.UserPreferences;
import de.danoeh.apexpod.core.storage.StatisticsItem;
import de.danoeh.apexpod.core.util.Converter;
import de.danoeh.apexpod.core.util.DateFormatter;
import de.danoeh.apexpod.model.stats.FeedPlayStats;
import de.danoeh.apexpod.model.stats.FeedPlayStatsItem;
import de.danoeh.apexpod.view.PieChartView;

import java.util.Date;
import java.util.List;

/**
 * Adapter for the playback statistics list.
 */
public class PlaybackStatisticsListAdapter extends StatisticsListAdapter {

    boolean countAll = true;

    public PlaybackStatisticsListAdapter(Context context) {
        super(context);
    }

    public void setCountAll(boolean countAll) {
        this.countAll = countAll;
    }

    @Override
    String getHeaderCaption() {
        long usageCounting = UserPreferences.getUsageCountingDateMillis();
        if (usageCounting > 0) {
            String date = DateFormatter.formatAbbrev(context, new Date(usageCounting));
            return context.getString(R.string.statistics_counting_since, date);
        } else {
            return context.getString(R.string.total_time_listened_to_podcasts);
        }
    }

    @Override
    String getHeaderValue() {
        return Converter.shortLocalizedDuration(context, (long) pieChartData.getSum());
    }

    @Override
    PieChartView.PieChartData generateChartData(FeedPlayStats feedPlayStats) {
        float[] dataValues = new float[feedPlayStats.size()];
        for (int i = 0; i < feedPlayStats.size(); i++) {
            FeedPlayStatsItem item = feedPlayStats.getItems().get(i);
            dataValues[i] = item.getTotalListeningTime();
        }
        return new PieChartView.PieChartData(dataValues);
    }

    @Override
    void onBindFeedViewHolder(StatisticsHolder holder, FeedPlayStatsItem statsItem) {
        long time = statsItem.getTotalListeningTime();
        holder.value.setText(Converter.shortLocalizedDuration(context, time));

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(statsItem.getFeed().getTitle());
            dialog.setMessage(context.getString(R.string.statistics_details_dialog,
                    statsItem.getEpisodesStarted(),
                    statsItem.getEpisodeCount(), Converter.shortLocalizedDuration(context,
                            statsItem.getTotalListeningTime()),
                    Converter.shortLocalizedDuration(context, statsItem.getTotalListeningTime())));
            dialog.setPositiveButton(android.R.string.ok, null);
            dialog.show();
        });
    }

}
