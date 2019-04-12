package com.alzheimersmate.almate;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public DrawerLayout mDrawerLayout;
    Button drawerButton;
    private MobileServiceClient mClient;
    private SensorManager sensorMan;
    private Sensor accelerometer;

    private float[] mGravity;
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;
    private double threshold_acc = 25;
    long prevAcTime = 0;
    long prevReTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new MenuActivity()).commit();

        /*try {
            mClient = new MobileServiceClient(
                    "https://alm8.azurewebsites.net",
                    this
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/

        /*TodoItem item = new TodoItem();
        item.Text = "Awesome item";
        mClient.getTable(TodoItem.class).insert(item, new TableOperationCallback<item>() {
            public void onCompleted(TodoItem entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    // Insert succeeded
                } else {
                    // Insert failed
                }
            }
        });*/

        AppCenter.start(getApplication(), "f22874de-e382-483a-9ec5-232193bd3ed3", Analytics.class);
        Analytics.trackEvent("Main Activity Opened");

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
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
                                //getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new AboutUsFragment()).commit();
                                String urlString2 = "http://amity.edu/ais/gurgaon46/";
                                Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString2));
                                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent2.setPackage("com.android.chrome");
                                try {
                                    MainActivity.this.startActivity(intent2);
                                } catch (ActivityNotFoundException ex) {
                                    // Chrome browser presumably not installed so allow user to choose instead
                                    intent2.setPackage(null);
                                    MainActivity.this.startActivity(intent2);
                                }
                                break;
                            case R.id.nav_info_alz:
                                //getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new AboutAlzFragment()).commit();
                                String urlString = "https://www.alz.org";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setPackage("com.android.chrome");
                                try {
                                    MainActivity.this.startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    // Chrome browser presumably not installed so allow user to choose instead
                                    intent.setPackage(null);
                                    MainActivity.this.startActivity(intent);
                                }
                                break;
                            default:
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_main_Activity, new MenuActivity()).commit();
                                break;
                        }
                        return true;
                    }
                });


        sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;



    }

    @Override
    public void onResume() {
        super.onResume();
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMan.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values.clone();
            // Shake detection
            double x = mGravity[0];
            double y = mGravity[1];
            double z = mGravity[2];

            /*if(x>xm) {
                xm = x;
                xmo.setText(String.valueOf(xm));
            }
            if(y>ym) {
                ym =y;
                ymo.setText(String.valueOf(ym));
            }
            if(z>zm) {
                zm = z;
                zmo.setText(String.valueOf(zm));
            }

            xac.setText(String.valueOf(x));
            yac.setText(String.valueOf(y));
            zac.setText(String.valueOf(z));*/

            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(x * x + y * y + z * z);;
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect


            //Change to mAccel for total acceleration


            /*if(z > threshold_acceleration) {
                if (System.currentTimeMillis() - prevAcTime < 5000) {
                    return;
                }

                prevAcTime = System.currentTimeMillis();

                Intent intent = new Intent(MainActivity.this, UserAccelerationWarn.class);
                intent.putExtra("acc", String.format("%.2f", z/10));
                startActivity(intent);

            }*/

            if(Math.abs(z) > threshold_acc) {
                if (System.currentTimeMillis() - prevReTime < 5000) {
                    return;
                }

                prevReTime = System.currentTimeMillis();

                Intent intent = new Intent(MainActivity.this, UserFallWarn.class);
                startActivity(intent);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
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
        /*Intent intent = new Intent(MainActivity.this, mainFragmentView.class);
        intent.putExtra("FragmentOpen", "people");
        startActivity(intent);*/
        String urlString = "https://azure.microsoft.com/en-in/services/cognitive-services/face/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            MainActivity.this.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null);
            MainActivity.this.startActivity(intent);
        }

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

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MainActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

}
