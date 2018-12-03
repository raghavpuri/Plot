package com.alzheimersmate.almate;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MedicinesAdd extends AppCompatActivity {

    static final int DIALOG_ID = 0;
    int hour_x, minute_x;
    Button setTime,addMed;
    TextView textTime;
    private SQLiteDatabase mDatabase;
    private EditText mEditMedName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines_add);
        textTime = (TextView) findViewById(R.id.med_add_time_text);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
        showTimePicker();
        addMed = (Button) findViewById(R.id.add_med);
        mEditMedName = (EditText) findViewById(R.id.edit_add_med);
        addMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID)
            return new TimePickerDialog(MedicinesAdd.this, kTimePickerListener, hour_x, minute_x, false);
        return null;
    }

    protected TimePickerDialog.OnTimeSetListener kTimePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    hour_x = hourOfDay;
                    minute_x = minute;
                    if(hour_x>12) {
                        if(hour_x>21) {
                            textTime.setText("" + (hour_x-12) + ":" + minute_x + " PM");
                        }
                        else {
                            textTime.setText("0" + (hour_x-12) + ":" + minute_x + " PM");
                        }
                    }
                    else{
                        if(hour_x>9) {
                            textTime.setText("" + hour_x + ":" + minute_x + " AM");
                        }
                        else {
                            textTime.setText("0" + hour_x + ":" + minute_x + " AM");
                        }
                    }
                    Toast.makeText(MedicinesAdd.this, "Time Set!", Toast.LENGTH_LONG).show();
                }
            };

    private void addItem() {

        if (mEditMedName.getText().toString().trim().length() == 0 || (textTime.getText().toString()).equals("Time")) {
            Toast.makeText(MedicinesAdd.this, "Set the Required Details", Toast.LENGTH_LONG).show();
            return;
        }

        String name = mEditMedName.getText().toString();
        String time = textTime.getText().toString();
        ContentValues cv = new ContentValues();
        cv.put(MedicineContract.MedicineEntry.COLUMN_MED_NAME, name);
        cv.put(MedicineContract.MedicineEntry.COLUMN_TIME, time);

        long result = mDatabase.insert(MedicineContract.MedicineEntry.TABLE_NAME, null, cv);
        if(result == -1) {
            Toast.makeText(MedicinesAdd.this, "There was some error :(", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(MedicinesAdd.this, "Medicine Added!", Toast.LENGTH_LONG).show();
        }

        mEditMedName.getText().clear();
        textTime.setText("Time");
        Intent intent = new Intent(MedicinesAdd.this, medicine_view.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void showTimePicker() {
        setTime = (Button) findViewById(R.id.med_add_time_btn);
        setTime.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DIALOG_ID);
                    }
                }
        );
    }

}
