package com.alzheimersmate.almate;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperation;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperationResult;
import com.microsoft.projectoxford.vision.contract.HandwritingTextLine;
import com.microsoft.projectoxford.vision.contract.HandwritingTextWord;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MedicinesAdd extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static final int DIALOG_ID = 0;
    int hour_x, minute_x;
    Button setTime,addMed,med_ocr_btn;
    TextView textTime;
    private SQLiteDatabase mDatabase;
    private EditText mEditMedName;
    Spinner dropdown, docdown;
    String[] items;
    String[] docs;
    int docID;
    TextInputLayout editLayout;
    private VisionServiceClient client;
    private Bitmap bitmap;
    private int retryCountThreshold = 30;
    private Uri mUriPhotoTaken;
    private File mFilePhotoTaken;
    private Uri imagUrl;

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
        items = new String[]{"Donepezil", "Rivastigmine", "Galantamine", "Other"};
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
        med_ocr_btn = (Button) findViewById(R.id.med_ocr_btn);
        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }
    }

    public void clickPic(View view) {
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }*/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        editLayout.setVisibility(View.VISIBLE);
        //Set equal to the index of OTHER if you add more medicines.
        dropdown.setSelection(3);
        if(intent.resolveActivity(getPackageManager()) != null) {
            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                mFilePhotoTaken = File.createTempFile(
                        "IMG_",  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
                // Create the File where the photo should go
                // Continue only if the File was successfully created
                if (mFilePhotoTaken != null) {
                    mUriPhotoTaken = FileProvider.getUriForFile(this,
                            "com.alzheimersmate.almate.fileprovider",
                            mFilePhotoTaken);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);

                    // Finally start camera activity
                    startActivityForResult(intent, 1);
                }
            } catch (IOException e) {
                e.getMessage();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    imagUrl = Uri.fromFile(mFilePhotoTaken);
                    /*Bundle extras = imageReturnedIntent.getExtras();
                    bitmap = (Bitmap) extras.get("data");*/
                    bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            imagUrl, getContentResolver());
                    if (bitmap != null) {
                        doRecognize();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void doRecognize() {
        med_ocr_btn.setEnabled(false);
        mEditMedName.setText("Analyzing...");

        try {
            new doRequest(this).execute();
        } catch (Exception e) {
            /*mEditMedName.setText("Error encountered. Exception is: " + e.toString());*/
        }
    }

    private String process() throws VisionServiceException, IOException, InterruptedException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray())) {
                //post image and got operation from API
                HandwritingRecognitionOperation operation = this.client.createHandwritingRecognitionOperationAsync(inputStream);

                HandwritingRecognitionOperationResult operationResult;
                //try to get recognition result until it finished.

                int retryCount = 0;
                do {
                    if (retryCount > retryCountThreshold) {
                        throw new InterruptedException("Can't get result after retry in time.");
                    }
                    Thread.sleep(1000);
                    operationResult = this.client.getHandwritingRecognitionOperationResultAsync(operation.Url());
                }
                while (operationResult.getStatus().equals("NotStarted") || operationResult.getStatus().equals("Running"));

                String result = gson.toJson(operationResult);
                Log.d("result", result);
                return result;

            } catch (Exception ex) {
                throw ex;
            }
        } catch (Exception ex) {
            throw ex;
        }

    }


    private static class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        private WeakReference<MedicinesAdd> recognitionActivity;

        public doRequest(MedicinesAdd activity) {
            recognitionActivity = new WeakReference<MedicinesAdd>(activity);
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                if (recognitionActivity.get() != null) {
                    return recognitionActivity.get().process();
                }
            } catch (Exception e) {
                this.e = e;    // Store error
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (recognitionActivity.get() == null) {
                return;
            }
            // Display based on error existence
            if (e != null) {
                recognitionActivity.get().mEditMedName.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                HandwritingRecognitionOperationResult r = gson.fromJson(data, HandwritingRecognitionOperationResult.class);

                StringBuilder resultBuilder = new StringBuilder();
                //if recognition result status is failed. display failed
                if (r.getStatus().equals("Failed")) {
                    resultBuilder.append("Error: Recognition Failed");
                } else {
                    for (HandwritingTextLine line : r.getRecognitionResult().getLines()) {
                        for (HandwritingTextWord word : line.getWords()) {
                            resultBuilder.append(word.getText() + " ");
                        }
                        resultBuilder.append("\n");
                    }
                    resultBuilder.append("\n");
                }

                recognitionActivity.get().mEditMedName.setText(resultBuilder);
            }
            recognitionActivity.get().med_ocr_btn.setEnabled(true);
        }
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
                    if(minute_x<10) {
                        textTime.setText(textTime.getText().toString().substring(0,3)+"0"+textTime.getText().toString().substring(3));
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
        //cv.put(MedicineContract.MedicineEntry.COLUMN_DOCTOR,docID);

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
