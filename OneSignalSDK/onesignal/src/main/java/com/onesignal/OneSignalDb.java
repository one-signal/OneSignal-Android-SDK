package com.onesignal;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

public interface OneSignalDb {

    Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy);

    Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit);

    void insert(String table, String nullColumnHack, ContentValues values);

    void insertOrThrow(String table, String nullColumnHack, ContentValues values)
            throws SQLException;

    int update(String table, ContentValues values, String whereClause, String[] whereArgs);

    void delete(String table, String whereClause, String[] whereArgs);
}
