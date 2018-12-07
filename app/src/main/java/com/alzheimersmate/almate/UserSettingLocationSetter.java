package com.alzheimersmate.almate;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
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


public class UserSettingLocationSetter extends AppCompatActivity
        implements OnMapReadyCallback {
    private SQLiteDatabase mDatabase;
    String latitude, longitude;
    double latilong, longilong;
    private FusedLocationProviderClient mFusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_places_add);
        placesDBHelper dbHelper = new placesDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserSettingLocationSetter.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            ActivityCompat.requestPermissions(UserSettingLocationSetter.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(UserSettingLocationSetter.this, new OnSuccessListener<Location>() {
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
        SharedPreferences pref = getApplicationContext().getSharedPreferences("ALMATEprefs", 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString("userLat", latitude);
        editor.putString("userLong", longitude);
        Toast.makeText(UserSettingLocationSetter.this,"HOME location saved!",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(UserSettingLocationSetter.this, MainActivity.class);
        startActivity(intent);
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

}
