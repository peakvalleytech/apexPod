package de.danoeh.apexpod.core.storage.database

import android.database.sqlite.SQLiteDatabase
import de.danoeh.apexpod.core.storage.ApexDBAdapter
import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.model.stats.FeedPlayStats
import de.danoeh.apexpod.model.stats.FeedPlayStatsItem

class FeedPlayStatsDao {
    private lateinit var db: SQLiteDatabase
    lateinit var dbAdapter: ApexDBAdapter
    lateinit var playStatDao: PlayStatDao
    init {
        dbAdapter = ApexDBAdapter.getInstance()
        db = dbAdapter.db
        playStatDao = PlayStatDao()
    }
    fun getFeedPlayStats() : FeedPlayStats? {
        val feeds = DBReader.getFeedList()
        val playStats = playStatDao.getAllPlayStats()
        val feedPlayStatsItemList = mutableListOf<FeedPlayStatsItem>()
        val feedIdToStatsItemMap = mutableMapOf<Long, FeedPlayStatsItem>()
        feeds.forEach {
            val feedPlayStatsItem = FeedPlayStatsItem(it)
            feedIdToStatsItemMap.put(it.id, feedPlayStatsItem)
        }

        playStats.


        return FeedPlayStats(feedPlayStatsItemList)
    }

}