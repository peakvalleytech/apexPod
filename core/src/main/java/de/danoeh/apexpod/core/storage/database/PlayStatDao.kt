package de.danoeh.apexpod.core.storage.database

import android.database.sqlite.SQLiteDatabase
import de.danoeh.apexpod.core.storage.ApexDBAdapter
import de.danoeh.apexpod.model.stats.PlayStat
import de.danoeh.apexpod.model.stats.PlayStatRange

class PlayStatDao {
    private var db: SQLiteDatabase
    var dbAdapter: ApexDBAdapter
    init {
        dbAdapter = ApexDBAdapter.getInstance()
        db = dbAdapter.db
    }
    fun createPlayStat(playStat : PlayStat) : Long {

    }
    fun getAllPlayStats() : PlayStatRange {
        return PlayStatRange(0, 1)
    }
    fun getAllByFeedItemId(feedItemId : Long) : PlayStatRange {
        return PlayStatRange(0, 1)
    }
    fun getPlayStatsByRange(fromDateMillis : Long, toDateMillis : Long) : PlayStatRange {
        return PlayStatRange(0, 1)
    }
    fun updatePlayStat(playStat : PlayStat) {

    }
    fun deletePlayStat(playStat: PlayStat) {

    }
}