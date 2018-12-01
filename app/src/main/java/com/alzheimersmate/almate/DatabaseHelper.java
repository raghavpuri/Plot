package com.alzheimersmate.almate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alzheimersmate.almate.MedicineContract.*;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "medicines.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MEDICINELIST_TABLE = "CREATE TABLE " +
                MedicineEntry.TABLE_NAME + " (" +
                MedicineEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MedicineEntry.COLUMN_MED_NAME + " TEXT NOT NULL, " +
                MedicineEntry.COLUMN_TIME + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_MEDICINELIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MedicineEntry.TABLE_NAME);
        onCreate(db);
    }
}
