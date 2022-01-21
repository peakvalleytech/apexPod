package de.danoeh.apexpod.core.storage.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import de.danoeh.apexpod.core.storage.ApexDBAdapter
import de.danoeh.apexpod.core.storage.PodDBAdapter
import de.danoeh.apexpod.model.stats.PlayStat
import de.danoeh.apexpod.model.stats.PlayStatRange
import java.sql.SQLException

class PlayStatDao {
    private var db: SQLiteDatabase
    var dbAdapter: ApexDBAdapter
    init {
        dbAdapter = ApexDBAdapter.getInstance()
        db = dbAdapter.db
    }
    fun createPlayStat(playStat : PlayStat) : Long {
        return doTransaction {
            val values = ContentValues()
            values.put(PodDBAdapter.KEY_FEEDITEM, playStat.feedItemId)
            values.put(PodDBAdapter.KEY_FEED, playStat.feedId)
            values.put(PodDBAdapter.KEY_START_TIME, playStat.startTime)
            values.put(PodDBAdapter.KEY_END_TIME, playStat.endTime)
            values.put(PodDBAdapter.KEY_START_POS, playStat.startPos)
            values.put(PodDBAdapter.KEY_END_POS, playStat.endPos)

            return@doTransaction  db.insertWithOnConflict(PodDBAdapter.TABLE_NAME_PLAYSTATS,
                null,
                values, SQLiteDatabase.CONFLICT_REPLACE)
        } as Long
    }
    fun getAllPlayStats() : PlayStatRange? {
        return doTransaction {
            val query : String = "SELECT * FROM ${PodDBAdapter.TABLE_NAME_PLAYSTATS}  ORDER BY ${PodDBAdapter.KEY_START_TIME}"
            var cursor : Cursor? = null
            var playStatRange : PlayStatRange? = null
            cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                var startTimeIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_START_TIME)
                val rangeStartTime = cursor.getLong(startTimeIndex)
                var rangeEndtime = 0L
                if (cursor.isLast) {
                    val endTimeIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_END_TIME)
                    rangeEndtime = cursor.getLong(startTimeIndex)
                } else {
                    cursor.moveToLast()
                    val endTimeIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_END_TIME)
                    rangeEndtime = cursor.getLong(startTimeIndex)
                }
                playStatRange = PlayStatRange(rangeStartTime, rangeEndtime)
                cursor.moveToFirst()
                do {
                    val idIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_ID)
                    val feedItemIdIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_FEEDITEM)
                    val feedIdIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_FEED)
                    startTimeIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_START_TIME)
                    val endTimeIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_END_TIME)
                    val startPosIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_START_POS)
                    val endPosIndex = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_END_POS)
                    val playStat = PlayStat(
                        cursor.getLong(idIndex),
                        cursor.getLong(feedItemIdIndex),
                        cursor.getLong(feedIdIndex),
                        cursor.getLong(startTimeIndex),
                        cursor.getLong(endTimeIndex),
                        cursor.getInt(startPosIndex),
                        cursor.getInt(endPosIndex))
                    playStatRange.add(playStat)
                } while (cursor.moveToNext())
            }
            cursor.close()
            return@doTransaction playStatRange as PlayStatRange?
        } as PlayStatRange?
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

    private fun doTransaction(transaction : () -> Any?) : Any? {
        try {
            db.beginTransaction()
            val retValue = transaction()
            db.setTransactionSuccessful()
            return retValue
        } catch (e: SQLException) {
            Log.e(this.javaClass.canonicalName, Log.getStackTraceString(e))
            throw e
        } finally {
            db.endTransaction()
        }
    }
}