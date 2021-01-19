package com.project.gpstracking;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class GPSServiceProvider extends ContentProvider {
    public static final String KEY_ID = "KEY_ID";
    public static final String KEY_USER_ID = "KEY_USER_ID";
    public static final String KEY_SESSION = "KEY_SESSION";
    public static final String KEY_TIME = "KEY_TIME";
    public static final String KEY_DISTANCE = "KEY_DISTANCE";

    private MySQLiteOpenHelper myOpenHelper;
    public static final Uri CONTENT_URI =
            Uri.parse("content://com.project.gpstracking.service/service");
    @Override
    public boolean onCreate() {
        myOpenHelper = new MySQLiteOpenHelper(getContext(),
                MySQLiteOpenHelper.DATABASE_NAME, null,
                MySQLiteOpenHelper.DATABASE_VERSION);
        return true;
    }
    private static final int ALLROWS = 1;
    private static final int SINGLE_ROW = 2;

    // kiem tra tinh dung dan cua uri
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.project.gpstracking", "service", ALLROWS);
        uriMatcher.addURI("com.project.gpstracking", "service/#", SINGLE_ROW);
    }
    @Override
    public String getType(Uri url) {
        switch (uriMatcher.match(url)) {
            case ALLROWS: return "vnd.android.cursor.dir/vnd.gpstracker.service";
            case SINGLE_ROW: return "vnd.android.cursor.item/vnd.gpstracker.service";
            default: throw new IllegalArgumentException("Unsupported URI: " + url);
        }
    }
    public Cursor query(Uri url, String[] projection, String selection,
                        String[] selectionArgs, String sort) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        String groupBy = null;
        String having = null;
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MySQLiteOpenHelper.DATABASE_TABLE);
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, groupBy, having, sort);
        return cursor;
    }
    @Override
    public Uri insert(Uri url, ContentValues values) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        String nullColumnHack = null;
        long id = db.insert(MySQLiteOpenHelper.DATABASE_TABLE,
                nullColumnHack, values);
        if (id > -1) {
            Uri insertedId = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(insertedId, null);
            return insertedId;
        }
        else
            return null;
    }

    @Override
    public int delete(Uri url, String selection, String[] selectionArgs) {
        try{
            SQLiteDatabase db = myOpenHelper.getWritableDatabase();
            if(selection == null)
                selection = "1";
            int deleteCount = db.delete(MySQLiteOpenHelper.DATABASE_TABLE, selection,
                    selectionArgs);
            getContext().getContentResolver().notifyChange(url, null);
            return deleteCount;
        }catch (Exception ex){
            Log.d(GPS.LOG_TAG, ex.getMessage());
        }
        return -1;
    }

    @Override
    public int update(Uri url, ContentValues values,
                      String selection, String[] selectionArgs) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        int updateCount = db.update(MySQLiteOpenHelper.DATABASE_TABLE,
                values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(url, null);
        return updateCount;
    }

    private static class MySQLiteOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "gpsTrackerServiceDatabase.db";
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_TABLE = "gpsTrackerServiceTable";
        public MySQLiteOpenHelper(Context context, String name,
                                  SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        private static final String DATABASE_CREATE = "create table " +
                DATABASE_TABLE + " (" + KEY_ID +
                " integer primary key autoincrement, " +
                KEY_SESSION + " text, " +
                KEY_USER_ID + " text, " +
                KEY_TIME + " text, " +
                KEY_DISTANCE + " text );";

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("TaskDBAdapter", "Upgrading from version " +
                    oldVersion + " to " +
                    newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}

