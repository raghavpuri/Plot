package com.alzheimersmate.almate;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    public DrawerLayout mDrawerLayout;
    Button drawerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new MenuActivity()).commit();

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerButton = (Button) findViewById(R.id.drawer_button);
        drawerButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDrawerLayout.openDrawer(Gravity.START);
                    }
                }
        );
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new MenuActivity()).commit();
                                break;
                            case R.id.nav_user_set:
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new UserSettingsFragment()).commit();
                                break;
                            case R.id.nav_about:
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new AboutUsFragment()).commit();
                                break;
                            case R.id.nav_info_alz:
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new AboutAlzFragment()).commit();
                                break;
                            default:
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new MenuActivity()).commit();
                                break;
                        }
                        return true;
                    }
                });
    }

    public void goto_medicinesview(View view) {
        Intent intent = new Intent(MainActivity.this, mainFragmentView.class);
        intent.putExtra("FragmentOpen", "medicine");
        startActivity(intent);/*, ActivityOptions.makeSceneTransitionAnimation(this).toBundle()*/
    }

    public void goto_placesview(View view) {
        Intent intent = new Intent(MainActivity.this, mainFragmentView.class);
        intent.putExtra("FragmentOpen", "place");
        startActivity(intent);
        /*Intent intent = new Intent(MainActivity.this, trycamera.class);
        startActivity(intent);*/
    }

    public void goto_peopleview(View view) {
        Intent intent = new Intent(MainActivity.this, mainFragmentView.class);
        intent.putExtra("FragmentOpen", "people");
        startActivity(intent);
    }

    public void saveUserLocation(View view) {
        Intent intent = new Intent(MainActivity.this, UserSettingLocationSetter.class);
        startActivity(intent);
    }

    public void saveUserSettings(View view) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("ALMATEprefs", 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText userName, userEmail, userNumber;
                Button saveUserSettings, saveUserCurrentLoc;
                String latitude = "";
                String longitude = "";
                userName = (EditText) findViewById(R.id.username);
                userEmail = (EditText) findViewById(R.id.useremail);
                userNumber = (EditText) findViewById(R.id.usernumber);
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (userName.getText().toString().trim().length() != 0) {
                            editor.putString("userName", userName.getText().toString().trim());
                        }
                        if (userEmail.getText().toString().trim().length() != 0) {
                            editor.putString("userEmail", userEmail.getText().toString().trim());
                        }
                        if (userNumber.getText().toString().trim().length() != 0) {
                            editor.putString("userNumber", userNumber.getText().toString().trim());
                        }
                        editor.apply();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new MenuActivity()).commit();
                        break;
                        case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to save these changes?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void pickPic(View view) {
        Intent intentpick = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intentpick , 0);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        ImageView imageview = (ImageView) findViewById(R.id.usersettingspicdisplay);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    Bitmap mbitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            selectedImage, getContentResolver());
                    imageview.setImageBitmap(mbitmap);
                    ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                    mbitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
                    byte [] b=baos.toByteArray();
                    String temp= Base64.encodeToString(b, Base64.DEFAULT);
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("ALMATEprefs", 0); // 0 - for private mode
                    final SharedPreferences.Editor editor = pref.edit();
                    editor.putString("userDisplayPic", temp);
                    editor.apply();
                    Toast.makeText(MainActivity.this,"Display Picture Changed!",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
