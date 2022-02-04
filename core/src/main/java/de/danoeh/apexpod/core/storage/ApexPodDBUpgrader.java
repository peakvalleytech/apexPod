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
            db.execSQL(PodDBAdapter.CREATE_TABLE_PLAYSTATS);
        }
    }

    static void downgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // This will occur in very early versions of ApexPod when the version was derived from
        // last AntennaPod version. In later versions, the version was reset to 1.
    }
}
