package de.danoeh.apexpod.core.storage.database

import android.database.sqlite.SQLiteDatabase
import de.danoeh.apexpod.core.storage.ApexDBAdapter
import de.danoeh.apexpod.model.stats.FeedPlayStats

class FeedPlayStatsDao {
    private lateinit var db: SQLiteDatabase
    lateinit var dbAdapter: ApexDBAdapter

    init {
        dbAdapter = ApexDBAdapter.getInstance()
        db = dbAdapter.db
    }
    fun getFeedPlayStats() : FeedPlayStats? {
        return null
    }
}