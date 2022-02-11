package de.danoeh.apexpod.core.storage.database

import android.database.sqlite.SQLiteDatabase

abstract class DBUpgrade(val oldversion : Int, val newversion : Int) {
    abstract fun upgrade(db : SQLiteDatabase)
}