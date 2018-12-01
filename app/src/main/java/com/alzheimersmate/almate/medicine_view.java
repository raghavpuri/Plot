package com.alzheimersmate.almate;

import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class medicine_view extends AppCompatActivity {
    private SQLiteDatabase mDatabase;
    private MedicineAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_view);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        RecyclerView recyclerView = findViewById(R.id.med_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MedicineAdapter(this, getAllItems());
        recyclerView.setAdapter(mAdapter);
        mAdapter.swapCursor(getAllItems());

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)  {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                removeItem((long) viewHolder.itemView.getTag());
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                mAdapter.swapCursor(getAllItems());
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(medicine_view.this);
                builder.setMessage("Do you want to delete this medicine schedule?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        }).attachToRecyclerView(recyclerView);

    }

    private void removeItem(long id) {
        mDatabase.delete(MedicineContract.MedicineEntry.TABLE_NAME,
                MedicineContract.MedicineEntry.COLUMN_ID + "=" + id, null);
        mAdapter.swapCursor(getAllItems());
    }

    private Cursor getAllItems() {
        return mDatabase.query(
                MedicineContract.MedicineEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MedicineContract.MedicineEntry.COLUMN_TIME + " DESC"
        );
    }

    public void goto_medicinesadd(View view) {
        Intent intent = new Intent(medicine_view.this, MedicinesAdd.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}
