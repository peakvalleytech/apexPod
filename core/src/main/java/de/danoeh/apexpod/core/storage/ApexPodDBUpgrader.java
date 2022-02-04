package de.danoeh.apexpod.core.storage;

import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.util.Log;

class ApexPodDBUpgrader {
    /**
     * Upgrades the given database to a new schema version
     */
    static void upgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (oldVersion < 2) {
        }
    }

    static void downgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // This should be called once for earlier versions that used AntennaPod versioning.
        // In later versions, the version was reset to 1, and should not be called
        // for these versions
        db.execSQL(PodDBAdapter.CREATE_TABLE_PLAYSTATS);
    }
}
