package com.alzheimersmate.almate;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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


public class PlacesAdd extends AppCompatActivity
        implements OnMapReadyCallback {
    Button addPlace;
    private SQLiteDatabase mDatabase;
    private EditText mEditPlaceName;
    String latitude, longitude;
    double latilong, longilong;
    private FusedLocationProviderClient mFusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_places_add);
        placesDBHelper dbHelper = new placesDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
        addPlace = (Button) findViewById(R.id.add_place);
        mEditPlaceName = (EditText) findViewById(R.id.editplacename);
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
