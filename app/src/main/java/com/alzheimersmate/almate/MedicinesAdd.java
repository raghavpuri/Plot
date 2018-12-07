package com.alzheimersmate.almate;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MedicinesAdd extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static final int DIALOG_ID = 0;
    int hour_x, minute_x;
    Button setTime,addMed;
    TextView textTime;
    private SQLiteDatabase mDatabase;
    private EditText mEditMedName;
    Spinner dropdown, docdown;
    String[] items;
    String[] docs;
    int docID;
    TextInputLayout editLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_medicines_add);
        textTime = (TextView) findViewById(R.id.med_add_time_btn);
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
        dropdown = findViewById(R.id.med_spinner);
        docdown = findViewById(R.id.doc_spinner);
        items = new String[]{"Med1", "Med2", "Med3", "Other"};
        docs = new String[]{"Doc1", "Doc2", "Doc3", "Doc4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MedicinesAdd.this, R.layout.med_spinner_item, items);
        ArrayAdapter<String> docadapter = new ArrayAdapter<>(MedicinesAdd.this, R.layout.med_spinner_item, docs);
        dropdown.setAdapter(adapter);
        docdown.setAdapter(docadapter);
        dropdown.setOnItemSelectedListener(MedicinesAdd.this);
        docdown.setOnItemSelectedListener(MedicinesAdd.this);
        dropdown.setSelection(0);
        docdown.setSelection(0);
        editLayout = (TextInputLayout) findViewById(R.id.med_edit_text_layout);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(((Spinner) parent).getId() == R.id.med_spinner) {
            switch (items[position]) {
                case "Other":
                    mEditMedName.setText("");
                    editLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    mEditMedName.setText(items[position]);
                    editLayout.setVisibility(View.INVISIBLE);
                    break;
            }
        }
        else {
            docID = position+1;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

        if (mEditMedName.getText().toString().trim().length() == 0 || (textTime.getText().toString()).equals("Select Time")) {
            Toast.makeText(MedicinesAdd.this, "Set the Required Details", Toast.LENGTH_LONG).show();
            return;
        }

        String name = mEditMedName.getText().toString();
        String time = textTime.getText().toString();
        String actualtime;
        if(time.substring(6,7).equals("P")) {
            actualtime = String.valueOf(Integer.parseInt(time.substring(0,2)) + 12) + time.substring(3,5);
        }
        else {
            actualtime = time.substring(0,2) + time.substring(3,5);
        }
        ContentValues cv = new ContentValues();
        cv.put(MedicineContract.MedicineEntry.COLUMN_MED_NAME, name);
        cv.put(MedicineContract.MedicineEntry.COLUMN_TIME, actualtime);
        cv.put(MedicineContract.MedicineEntry.COLUMN_DOCTOR,docID);

        long result = mDatabase.insert(MedicineContract.MedicineEntry.TABLE_NAME, null, cv);
        if(result == -1) {
            Toast.makeText(MedicinesAdd.this, "There was some error :(", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(MedicinesAdd.this, "Medicine Added!", Toast.LENGTH_LONG).show();
        }

        mEditMedName.getText().clear();
        textTime.setText("Select Time");
        dropdown.setSelection(0);
        docdown.setSelection(0);
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

    public void goback_med_view(View view) {
        Intent intent = new Intent(MedicinesAdd.this,mainFragmentView.class);
        intent.putExtra("FragmentOpen","medicine");
        startActivity(intent);/*, ActivityOptions.makeSceneTransitionAnimation(this).toBundle()*/
    }
}
