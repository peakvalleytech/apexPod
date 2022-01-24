package de.danoeh.apexpod.core.storage.database

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.sql.SQLException

fun SQLiteDatabase.doTransaction(transaction : (SQLiteDatabase) -> Any?) : Any? {
        try {
            this.beginTransaction()
            val retValue = transaction(this)
            this.setTransactionSuccessful()
            return retValue
        } catch (e: SQLException) {
            Log.e(this.javaClass.canonicalName, Log.getStackTraceString(e))
            throw e
        } finally {
            this.endTransaction()
        }
}