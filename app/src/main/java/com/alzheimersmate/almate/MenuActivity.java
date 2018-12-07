package com.alzheimersmate.almate;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MenuActivity extends Fragment {

    private SQLiteDatabase mDatabase;
    TextView userNameWisher;
    ImageView userDisplayPic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_menu, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        mDatabase = dbHelper.getWritableDatabase();
        userNameWisher = (TextView) getActivity().findViewById(R.id.user_name_menu_act);
        userDisplayPic = (ImageView) getActivity().findViewById(R.id.menuuserdisplaypic);
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("ALMATEprefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        userNameWisher.setText("Hey " + pref.getString("userName",null) + "!");
        String displaypicstring =  pref.getString("userDisplayPic", null);
        try {
            byte [] encodeByte= Base64.decode(displaypicstring,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            userDisplayPic.setImageBitmap(bitmap);
        } catch(Exception e) {
            e.getMessage();
        }
        Cursor medtop2 = mDatabase.query(
                MedicineContract.MedicineEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                MedicineContract.MedicineEntry.COLUMN_TIME + " ASC"
        );

        long mednum = DatabaseUtils.queryNumEntries(mDatabase, MedicineContract.MedicineEntry.TABLE_NAME);

        int i=0;
        while(true) {
            try {
                int disttime1, disttime2;
                if(!medtop2.moveToPosition(i)) {
                    medtop2.moveToPosition(0);
                    int tabletime = Integer.parseInt(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_TIME)));
                    Date currentLocalTime = Calendar.getInstance().getTime();
                    DateFormat date = new SimpleDateFormat("HHmm");
                    String localTime = date.format(currentLocalTime);
                    int currtime = Integer.parseInt(localTime);
                    disttime1 = (tabletime-currtime)/100;
                    if(disttime1<0) {
                        disttime1 += 24;
                    }
                    ((TextView) getView().findViewById(R.id.menu_med_1_med_time)).setText("In " + disttime1 + " Hours");
                    ((TextView) getView().findViewById(R.id.menu_med_1_med_name)).setText(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MED_NAME)));
                    if(mednum >= 2){
                        medtop2.moveToPosition(1);
                        tabletime = Integer.parseInt(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_TIME)));
                        disttime2 = (tabletime-currtime)/100;
                        if(disttime2<0) {
                            disttime2 += 24;
                        }
                        ((TextView) getView().findViewById(R.id.menu_med_2_med_time)).setText("In " + disttime2 + " Hours");
                        ((TextView) getView().findViewById(R.id.menu_med_2_med_name)).setText(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MED_NAME)));
                    }
                    else {
                        medtop2.moveToPosition(0);
                        tabletime = Integer.parseInt(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_TIME)));
                        disttime2 = (tabletime-currtime)/100;
                        if(disttime2<0) {
                            disttime2 += 24;
                        }
                        ((TextView) getView().findViewById(R.id.menu_med_2_med_time)).setText("In " + disttime2 + " Hours");
                        ((TextView) getView().findViewById(R.id.menu_med_2_med_name)).setText(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MED_NAME)));
                    }
                    break;
                }
                medtop2.moveToPosition(i);
                Date currentLocalTime = Calendar.getInstance().getTime();
                DateFormat date = new SimpleDateFormat("HHmm");
                String localTime = date.format(currentLocalTime);
                int currtime = Integer.parseInt(localTime);
                int tabletime = Integer.parseInt(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_TIME)));
                /*((TextView) findViewById(R.id.menu_med_1_med_name)).setText(String.valueOf(tabletime));*/
                if(tabletime>currtime) {
                    if(i!=mednum-1){
                        ((TextView) getView().findViewById(R.id.menu_med_1_med_name)).setText(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MED_NAME)));
                        disttime1 = (tabletime-currtime)/100;
                        ((TextView) getView().findViewById(R.id.menu_med_1_med_time)).setText("In " + disttime1 + " Hours");
                        medtop2.moveToPosition(i+1);
                        tabletime = Integer.parseInt(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_TIME)));
                        disttime2 = (tabletime-currtime)/100;
                        ((TextView) getView().findViewById(R.id.menu_med_2_med_time)).setText("In " + disttime2 + " Hours");
                        ((TextView) getView().findViewById(R.id.menu_med_2_med_name)).setText(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MED_NAME)));
                        break;
                    }
                    else{
                        disttime1 = (tabletime-currtime)/100;
                        ((TextView) getView().findViewById(R.id.menu_med_1_med_time)).setText("In " + disttime1 + " Hours");
                        ((TextView) getView().findViewById(R.id.menu_med_1_med_name)).setText(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MED_NAME)));
                        medtop2.moveToPosition(0);
                        tabletime = Integer.parseInt(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_TIME)));
                        disttime2 = (tabletime-currtime)/100;
                        if(disttime2<0) {
                            disttime2 += 24;
                        }
                        ((TextView) getView().findViewById(R.id.menu_med_2_med_time)).setText("In " + disttime2 + " Hours");
                        ((TextView) getView().findViewById(R.id.menu_med_2_med_name)).setText(medtop2.getString(medtop2.getColumnIndex(MedicineContract.MedicineEntry.COLUMN_MED_NAME)));
                        break;
                    }
                }
                i++;
            } catch (Exception e) {
                ((TextView) getView().findViewById(R.id.menu_med_1_med_name)).setText("Set atleast 1 medicine");
                ((TextView) getView().findViewById(R.id.menu_med_2_med_name)).setText("Set atleast 1 medicines");
            }
        }
    }
}
