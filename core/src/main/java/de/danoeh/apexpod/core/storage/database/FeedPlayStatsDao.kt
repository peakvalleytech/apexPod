package de.danoeh.apexpod.core.storage.database

import android.database.sqlite.SQLiteDatabase
import de.danoeh.apexpod.core.storage.ApexDBAdapter
import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.core.storage.FeedStatsCalculator
import de.danoeh.apexpod.model.feed.Feed
import de.danoeh.apexpod.model.feed.FeedItem
import de.danoeh.apexpod.model.stats.FeedPlayStats
import de.danoeh.apexpod.model.stats.FeedPlayStatsItem
import de.danoeh.apexpod.model.stats.PlayStatRange

class FeedPlayStatsDao {
    private var db: SQLiteDatabase
    var dbAdapter: ApexDBAdapter
    var playStatDao: PlayStatDao

    init {
        dbAdapter = ApexDBAdapter.getInstance()
        db = dbAdapter.db
        playStatDao = PlayStatDao()
    }

    fun getFeedPlayStats() : FeedPlayStats? {
        val feeds = DBReader.getFeedList()
        val playStats = playStatDao.getAllPlayStats()
        val feedIdToRangeMap = mutableMapOf<Long, PlayStatRange>()
        feeds.forEach {
            feedIdToRangeMap.put(it.id, PlayStatRange())
        }

        playStats?.forEach {
            val playStatRange = feedIdToRangeMap.get(it.feedId)
            playStatRange.let {
                psrIt ->
                    psrIt?.add(it)
            }
        }

        return FeedPlayStats(createFeedPlayStatsItem(feeds, feedIdToRangeMap))
    }

    private fun createFeedPlayStatsItem(
        feeds: List<Feed>,
        feedIdToRangeMap : Map<Long, PlayStatRange>) : MutableList<FeedPlayStatsItem> {
        val feedPlayStatsItemList = mutableListOf<FeedPlayStatsItem>()

        feeds.forEach {
            val feedStatsCalculator = FeedStatsCalculator()
            feedStatsCalculator.calculate(it.id)

            val playStatRange = feedIdToRangeMap.get(it.id)
            if (playStatRange != null) {
                val feedPlayStatsItem = FeedPlayStatsItem(
                    it,
                    totalSpeedAdjustedListeningTime = playStatRange.getTotalDuration(),
                    totalListeningTime = playStatRange.getTotalTime(),
                    0,
                    episodesStarted = feedStatsCalculator.episodesStarted,
                    episodeCount = feedStatsCalculator.episodes,
                    totalDownloadSize = feedStatsCalculator.totalDownloadSize,
                    downloadsCount = feedStatsCalculator.episodesDownloadCount
                )
                feedPlayStatsItemList.add(feedPlayStatsItem)
            }
        }

        return feedPlayStatsItemList
    }
}