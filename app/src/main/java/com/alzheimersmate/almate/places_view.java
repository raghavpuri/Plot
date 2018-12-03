package com.alzheimersmate.almate;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

public class places_view extends AppCompatActivity {
    private SQLiteDatabase mDatabase;
    private PlacesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_view);
        placesDBHelper dbHelper = new placesDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        RecyclerView recyclerView = findViewById(R.id.place_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlacesAdapter(this, getAllItems());
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
                TextView checkhomename = (TextView) viewHolder.itemView.findViewById(R.id.textview_placename_item);
                /*if(checkhomename.getText().equals("HOME")) {
                    mAdapter.swapCursor(getAllItems());
                    AlertDialog.Builder builder = new AlertDialog.Builder(places_view.this);
                    builder.setMessage("You cannot delete HOME").setCancelable(true).show();
                }
                else{}*/
                if(true) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    removeItem((long) viewHolder.itemView.getTag());
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    mAdapter.swapCursor(getAllItems());
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(places_view.this);
                    builder.setMessage("Do you want to delete this saved place?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void removeItem(long id) {
        mDatabase.delete(PlacesContract.PlacesEntry.TABLE_NAME,
                PlacesContract.PlacesEntry.COLUMN_ID + "=" + id, null);
        mAdapter.swapCursor(getAllItems());
    }

    private Cursor getAllItems() {
        return mDatabase.query(
                PlacesContract.PlacesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                PlacesContract.PlacesEntry.COLUMN_PLACE_NAME + " DESC"
        );
    }

    public void goto_placesadd(View view) {
        Intent intent = new Intent(places_view.this, PlacesAdd.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }



}
