package com.onesignal;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class OSInAppMessageRepository {

    private static final long SIX_MONTHS_TIME_SECONDS = 6 * 30 * 24 * 60 * 60;
    private final OneSignalDbHelper dbHelper;

    OSInAppMessageRepository(OneSignalDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Remove IAMs that the last display time was six month ago
     */
    @WorkerThread
    void deleteOldInAppMessages(long dateInSeconds) {
        SQLiteDatabase readableDb = dbHelper.getReadableDbWithRetries();
        Cursor cursor = readableDb.query(
                OneSignalDbContract.InAppMessageTable.TABLE_NAME,
                null,
                OneSignalDbContract.InAppMessageTable.COLUMN_NAME_LAST_DISPLAY + "<?",
                new String[]{String.valueOf(dateInSeconds - SIX_MONTHS_TIME_SECONDS)},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                String idToRemove = cursor.getString(cursor.getColumnIndex(OneSignalDbContract.InAppMessageTable.COLUMN_NAME_MESSAGE_ID));
                deleteInAppMessage(idToRemove);
            } while (cursor.moveToNext());
        }

        if (!cursor.isClosed())
            cursor.close();
    }

    @WorkerThread
    synchronized void deleteInAppMessage(String messageId) {
        SQLiteDatabase writableDb = dbHelper.getWritableDbWithRetries();

        try {
            writableDb.beginTransaction();
            writableDb.delete(OneSignalDbContract.InAppMessageTable.TABLE_NAME,
                    OneSignalDbContract.InAppMessageTable.COLUMN_NAME_MESSAGE_ID + " = ?", new String[]{messageId});
            writableDb.setTransactionSuccessful();
        } catch (Throwable t) {
            OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Error deleting in app message! ", t);
        } finally {
            if (writableDb != null) {
                try {
                    writableDb.endTransaction(); // May throw if transaction was never opened or DB is full.
                } catch (Throwable t) {
                    OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Error closing transaction! ", t);
                }
            }
        }
    }

    @WorkerThread
    synchronized void saveInAppMessage(OSInAppMessage inAppMessage) {
        SQLiteDatabase writableDb = dbHelper.getWritableDbWithRetries();

        ContentValues values = new ContentValues();
        values.put(OneSignalDbContract.InAppMessageTable.COLUMN_NAME_MESSAGE_ID, inAppMessage.messageId);
        values.put(OneSignalDbContract.InAppMessageTable.COLUMN_NAME_DISPLAY_QUANTITY, inAppMessage.getDisplayStats().getDisplayQuantity());
        values.put(OneSignalDbContract.InAppMessageTable.COLUMN_NAME_LAST_DISPLAY, inAppMessage.getDisplayStats().getLastDisplayTime());
        values.put(OneSignalDbContract.InAppMessageTable.COLUMN_CLICK_IDS, inAppMessage.getClickedClickIds().toString());
        values.put(OneSignalDbContract.InAppMessageTable.COLUMN_DISPLAYED, inAppMessage.isDisplayedInSession());

        int rowsUpdated = writableDb.update(OneSignalDbContract.InAppMessageTable.TABLE_NAME, values,
                OneSignalDbContract.InAppMessageTable.COLUMN_NAME_MESSAGE_ID + " = ?", new String[]{inAppMessage.messageId});
        if (rowsUpdated == 0)
            writableDb.insert(OneSignalDbContract.InAppMessageTable.TABLE_NAME, null, values);
        writableDb.close();
    }

    @WorkerThread
    synchronized List<OSInAppMessage> getAllInAppMessages() {
        List<OSInAppMessage> iams = new ArrayList<>();
        Cursor cursor = null;

        try {
            SQLiteDatabase readableDb = dbHelper.getReadableDbWithRetries();
            cursor = readableDb.query(
                    OneSignalDbContract.InAppMessageTable.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    String messageId = cursor.getString(cursor.getColumnIndex(OneSignalDbContract.InAppMessageTable.COLUMN_NAME_MESSAGE_ID));
                    String clickIds = cursor.getString(cursor.getColumnIndex(OneSignalDbContract.InAppMessageTable.COLUMN_CLICK_IDS));
                    int displayQuantity = cursor.getInt(cursor.getColumnIndex(OneSignalDbContract.InAppMessageTable.COLUMN_NAME_DISPLAY_QUANTITY));
                    long lastDisplay = cursor.getLong(cursor.getColumnIndex(OneSignalDbContract.InAppMessageTable.COLUMN_NAME_LAST_DISPLAY));
                    boolean displayed = cursor.getInt(cursor.getColumnIndex(OneSignalDbContract.InAppMessageTable.COLUMN_DISPLAYED)) == 1;

                    JSONArray clickIdsArray = new JSONArray(clickIds);
                    Set<String> clickIdsSet = new HashSet<>();

                    for (int i = 0; i < clickIdsArray.length(); i++) {
                        clickIdsSet.add(clickIdsArray.getString(i));
                    }

                    OSInAppMessage inAppMessage = new OSInAppMessage(messageId, clickIdsSet, displayed, new OSInAppMessageDisplayStats(displayQuantity, lastDisplay));
                    iams.add(inAppMessage);
                } while (cursor.moveToNext());
            }
        } catch (JSONException e) {
            OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Generating JSONArray from iam click ids:JSON Failed.", e);
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }

        return iams;
    }

}
