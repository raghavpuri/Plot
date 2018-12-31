package com.alzheimersmate.almate;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class mainFragmentView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fragment_view);
        BottomNavigationView bottomnav = (BottomNavigationView) findViewById(R.id.bottom_navigation_bar);
        bottomnav.setOnNavigationItemSelectedListener(navListener);
        switch (getIntent().getStringExtra("FragmentOpen")) {
            case "medicine":
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main, new FragmentMedicineView()).commit();
                bottomnav.getMenu().findItem(R.id.nav_bottom_med).setChecked(true);
                break;
            case "place":
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main, new FragmentPlacesView()).commit();
                bottomnav.getMenu().findItem(R.id.nav_bottom_place).setChecked(true);
                break;
            case "people":
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main, new FragmentPeopleView()).commit();
                bottomnav.getMenu().findItem(R.id.nav_bottom_people).setChecked(true);
                break;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_bottom_med:
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main, new FragmentMedicineView()).commit();
                            break;
                        case R.id.nav_bottom_place:
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main, new FragmentPlacesView()).commit();
                            break;
                        case R.id.nav_bottom_people:
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main, new FragmentPeopleView()).commit();
                            break;
                        case R.id.nav_bottom_home:
                            Intent intent = new Intent(mainFragmentView.this, MainActivity.class);
                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mainFragmentView.this).toBundle());
                            break;
                    }
                    return true;
                }
            };

    public void goto_medicinesadd(View view) {
        Intent intent = new Intent(this, MedicinesAdd.class);
        startActivity(intent);/*, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle()*/
    }

    public void goto_placesadd(View view) {
        Intent intent = new Intent(this, PlacesAdd.class);
        startActivity(intent);/*, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle()*/
    }

    public void takemehomepls(View view) {
        try{
            SharedPreferences pref = getApplicationContext().getSharedPreferences("ALMATEprefs", 0); // 0 - for private mode
            String latitude, longitude;
            latitude = pref.getString("userLat",null);
            longitude = pref.getString("userLong",null);
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+ latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mainFragmentView.this.startActivity(mapIntent);
        } catch (Exception e) {
            Toast.makeText(mainFragmentView.this, "Please configure all User Settings!",Toast.LENGTH_LONG).show();
        }

    }

}
