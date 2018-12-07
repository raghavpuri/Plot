package com.alzheimersmate.almate;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

public class UserSettingsFragment extends Fragment {
    ImageView displaypicuseredit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("ALMATEprefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        displaypicuseredit = (ImageView) getActivity().findViewById(R.id.usersettingspicdisplay);
        String displaypicstring =  pref.getString("userDisplayPic", null);
        try {
            byte [] encodeByte= Base64.decode(displaypicstring,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            displaypicuseredit.setImageBitmap(bitmap);
        } catch(Exception e) {
            e.getMessage();
        }
    }

}
