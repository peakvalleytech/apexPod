package de.danoeh.apexpod.core.storage.database

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import de.danoeh.apexpod.core.event.PlayListItemUpdate
import de.danoeh.apexpod.core.storage.ApexDBAdapter
import de.danoeh.apexpod.core.storage.DBReader
import de.danoeh.apexpod.core.storage.PodDBAdapter
import de.danoeh.apexpod.model.feed.FeedItem
import org.greenrobot.eventbus.EventBus

class PlayListItemDao() {
    private lateinit var db: SQLiteDatabase
    lateinit var dbAdapter: ApexDBAdapter

    init {
        dbAdapter = ApexDBAdapter.getInstance()
        db = dbAdapter.db
    }
    fun addItemsByPlayistId(id: Long, items : List<FeedItem>) {
        try {
            db.beginTransactionNonExclusive()
            for (item in items) {
                val values = ContentValues()
                values.put(PodDBAdapter.KEY_PLAYLIST, id)
                values.put(PodDBAdapter.KEY_FEEDITEM, item.id)
                db.insertWithOnConflict(
                    PodDBAdapter.TABLE_NAME_PLAYLIST_ITEMS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            EventBus.getDefault().post(PlayListItemUpdate())
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            Log.e(this.javaClass.canonicalName, Log.getStackTraceString(e))
        } finally {
            db.endTransaction()
        }
    }
    fun getItemsByPlayListId(id : Long) : List<FeedItem>? {
        val query : String = "${PodDBAdapter.SELECT_FEED_ITEMS_AND_MEDIA} INNER JOIN ${PodDBAdapter.TABLE_NAME_PLAYLIST_ITEMS} " +
                "ON ${PodDBAdapter.SELECT_KEY_ITEM_ID}  =  + ${PodDBAdapter.TABLE_NAME_PLAYLIST_ITEMS}.${PodDBAdapter.KEY_FEEDITEM}" +
                " WHERE " + PodDBAdapter.KEY_PLAYLIST + " = " + "?" +
                " ORDER BY " + PodDBAdapter.TABLE_NAME_PLAYLIST_ITEMS + "." + PodDBAdapter.KEY_ID
        var cursor : Cursor? = null
        var items : List<FeedItem>? = null
        try {
            db.beginTransactionNonExclusive()
            cursor = db.rawQuery(query, arrayOf(id.toString()))
            items = DBReader.extractItemlistFromCursor(PodDBAdapter.getInstance(), cursor)
            DBReader.loadAdditionalFeedItemListData(items)

            db.setTransactionSuccessful()
            db.endTransaction()
        } catch (e : SQLException) {
            android.util.Log.e(this.javaClass.canonicalName, android.util.Log.getStackTraceString(e))
            throw(e)
        } finally {
            cursor?.close()
        }

        return items
    }

    fun deleteItemByPlayListId(id: Long, item: FeedItem) {
        try {
            db.beginTransactionNonExclusive()
            db.delete(PodDBAdapter.TABLE_NAME_PLAYLIST_ITEMS, PodDBAdapter.KEY_PLAYLIST + " = ?" +
            " AND " + PodDBAdapter.KEY_FEEDITEM + " IN (?)", arrayOf<String>(id.toString(), item.id.toString()) )
            db.setTransactionSuccessful()
            EventBus.getDefault().post(PlayListItemUpdate())
        } catch (e : SQLException) {
            android.util.Log.e(this.javaClass.canonicalName, android.util.Log.getStackTraceString(e))
            throw(e)
        } finally {
            db.endTransaction()
        }
    }
}