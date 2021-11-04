package de.danoeh.apexpod.core.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.apexpod.model.Playlist;

public class ApexDBAdapter extends PodDBAdapter {
    private static final String TAG = "ApexDBAdapter";
    private static ApexDBAdapter instance;
    public static synchronized ApexDBAdapter getInstance() {
        if (instance == null) {
            instance = new ApexDBAdapter();
        }
        return instance;
    }
    public List<Playlist> getAllPlaylist() {
        String query = "SELECT * FROM playlists";
        List<Playlist> playlists;
        try (Cursor cursor = db.rawQuery(query, null)) {
            playlists = new ArrayList<>();
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(("id"));
                int nameColIndex = cursor.getColumnIndex("name");
                Playlist playlist = new Playlist(cursor.getString(nameColIndex));
                playlist.setId(cursor.getLong(idIndex));
                playlists.add(playlist);
            }
        }
        return playlists;
    }

    public void createPlaylist(Playlist playlist) {
        ContentValues values = new ContentValues();
        try {
            db.beginTransactionNonExclusive();
            values.put(KEY_PLAYLIST_NAME, playlist.getName());
            db.insertWithOnConflict(TABLE_NAME_PLAYLIST, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
        }
    }

    public int deletePlaylist(long id) {
        int rowsAffected = 0;
        try {
            db.beginTransactionNonExclusive();
            rowsAffected = db.delete(TABLE_NAME_PLAYLIST, "id = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            db.endTransaction();
        }
        return rowsAffected;
    }



}
