package com.alzheimersmate.almate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class placesDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "places.db";
    public static final int DATABASE_VERSION = 1;

    public placesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PLACELIST_TABLE = "CREATE TABLE " +
                PlacesContract.PlacesEntry.TABLE_NAME + " (" +
                PlacesContract.PlacesEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PlacesContract.PlacesEntry.COLUMN_PLACE_NAME + " TEXT NOT NULL, " +
                PlacesContract.PlacesEntry.COLUMN_LATITUDE + " TEXT NOT NULL, " +
                PlacesContract.PlacesEntry.COLUMN_LONGITUDE + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_PLACELIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlacesContract.PlacesEntry.TABLE_NAME);
        onCreate(db);
    }
}
