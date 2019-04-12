package com.alzheimersmate.almate;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperation;
import com.microsoft.projectoxford.vision.contract.HandwritingRecognitionOperationResult;
import com.microsoft.projectoxford.vision.contract.HandwritingTextLine;
import com.microsoft.projectoxford.vision.contract.HandwritingTextWord;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;


public class PlacesAdd extends AppCompatActivity
        implements OnMapReadyCallback {
    Button addPlace,place_ocr_btn;
    private SQLiteDatabase mDatabase;
    private EditText mEditPlaceName;
    String latitude, longitude;
    double latilong, longilong;
    private FusedLocationProviderClient mFusedLocationClient;
    private VisionServiceClient client;
    private Bitmap bitmap;
    private Uri mUriPhotoTaken;
    private File mFilePhotoTaken;
    private Uri imagUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_places_add);
        placesDBHelper dbHelper = new placesDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
        addPlace = (Button) findViewById(R.id.add_place);
        mEditPlaceName = (EditText) findViewById(R.id.editplacename);
        place_ocr_btn = (Button) findViewById(R.id.place_ocr_btn);
        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }
        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PlacesAdd.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            ActivityCompat.requestPermissions(PlacesAdd.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(PlacesAdd.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            latitude = String.valueOf(location.getLatitude());
                            longitude = String.valueOf(location.getLongitude());
                            latilong = location.getLatitude();
                            longilong = location.getLongitude();
                        }
                    }
                });
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_map);
        mapFragment.getMapAsync(this);
    }

    public void clickPic(View view) {
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }*/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
        place_ocr_btn.setEnabled(false);
        mEditPlaceName.setText("Analyzing...");

        try {
            new doRequest().execute();
        } catch (Exception e)
        {
            /*mEditPlaceName.setText("Error encountered. Exception is: " + e.toString());*/
        }
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.AutoDetect, true);

        String result = gson.toJson(ocr);
        Log.d("result", result);

        return result;
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                /*mEditPlaceName.setText("Error: " + e.getMessage());*/
                this.e = null;
            } else {
                Gson gson = new Gson();
                OCR r = gson.fromJson(data, OCR.class);

                String result = "";
                for (Region reg : r.regions) {
                    for (Line line : reg.lines) {
                        for (Word word : line.words) {
                            result += word.text + " ";
                        }
                        result += "\n";
                    }
                    result += "\n\n";
                }

                mEditPlaceName.setText(result);
            }
            place_ocr_btn.setEnabled(true);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        // Add a marker at our place,
        // and move the map's camera to the same location.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LatLng ourplace = new LatLng(latilong, longilong);
                googleMap.addMarker(new MarkerOptions().position(ourplace)
                        .title("Marker at our location"));
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ourplace, 17));
            }
        }, 500);
    }

    private void addItem() {

        if (mEditPlaceName.getText().toString().trim().length() == 0) {
            Toast.makeText(PlacesAdd.this, "Set the Required Details", Toast.LENGTH_LONG).show();
            return;
        }

        String name = mEditPlaceName.getText().toString();
        ContentValues cv = new ContentValues();
        cv.put(PlacesContract.PlacesEntry.COLUMN_PLACE_NAME, name);
        cv.put(PlacesContract.PlacesEntry.COLUMN_LATITUDE, latitude);
        cv.put(PlacesContract.PlacesEntry.COLUMN_LONGITUDE, longitude);

        long result = mDatabase.insert(PlacesContract.PlacesEntry.TABLE_NAME, null, cv);
        if(result == -1) {
            Toast.makeText(PlacesAdd.this, "There was some error :(", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(PlacesAdd.this, "Place Saved!", Toast.LENGTH_LONG).show();
        }
    }

    public void goback_place_view(View view) {
        Intent intent = new Intent(PlacesAdd.this,mainFragmentView.class);
        intent.putExtra("FragmentOpen","place");
        startActivity(intent);/*, ActivityOptions.makeSceneTransitionAnimation(this).toBundle()*/
    }

}
