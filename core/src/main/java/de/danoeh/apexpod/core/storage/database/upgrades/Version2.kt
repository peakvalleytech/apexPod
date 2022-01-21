package de.danoeh.apexpod.core.storage.database.upgrades

import android.database.sqlite.SQLiteDatabase
import de.danoeh.apexpod.core.storage.database.DBUpgrade

class Version2(val oldVersion : Int, val newVersion : Int) : DBUpgrade(oldVersion, newVersion) {
    override fun upgrade(db: SQLiteDatabase) {

    }
}