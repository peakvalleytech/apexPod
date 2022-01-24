package de.danoeh.apexpod.adapter;

import android.content.Context;
import android.text.format.Formatter;

import java.util.List;
import java.util.Locale;

import de.danoeh.apexpod.R;
import de.danoeh.apexpod.core.storage.StatisticsItem;
import de.danoeh.apexpod.model.stats.FeedPlayStats;
import de.danoeh.apexpod.model.stats.FeedPlayStatsItem;
import de.danoeh.apexpod.view.PieChartView;

/**
 * Adapter for the download statistics list.
 */
public class DownloadStatisticsListAdapter extends StatisticsListAdapter {

    public DownloadStatisticsListAdapter(Context context) {
        super(context);
    }

    @Override
    String getHeaderCaption() {
        return context.getString(R.string.total_size_downloaded_podcasts);
    }

    @Override
    String getHeaderValue() {
        return Formatter.formatShortFileSize(context, (long) pieChartData.getSum());
    }

    @Override
    PieChartView.PieChartData generateChartData(FeedPlayStats statisticsData) {
        float[] dataValues = new float[statisticsData.size()];
        for (int i = 0; i < statisticsData.size(); i++) {
            FeedPlayStatsItem item = statisticsData.getItems().get(i);
            dataValues[i] = item.getTotalDownloadSize();
        }
        return new PieChartView.PieChartData(dataValues);
    }

    @Override
    void onBindFeedViewHolder(StatisticsHolder holder, FeedPlayStatsItem item) {
        holder.value.setText(Formatter.formatShortFileSize(context, item.getTotalDownloadSize())
                + " • "
                + String.format(Locale.getDefault(), "%d%s",
                item.getDownloadsCount(), context.getString(R.string.episodes_suffix)));
    }

}
