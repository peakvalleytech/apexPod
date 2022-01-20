package de.danoeh.apexpod.core.storage.database.upgrades

import android.database.sqlite.SQLiteDatabase
import de.danoeh.apexpod.core.storage.database.DBUpgrade

class Version1 : DBUpgrade {
    override fun version() : Int {
        return 1
    }

    override fun upgrade(db: SQLiteDatabase, from: Int) {
    }
}