package com.alzheimersmate.almate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

public class UserFallWarn extends AppCompatActivity {

    TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_fall_warn);
        mTTS = new TextToSpeech(UserFallWarn.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    int result = mTTS.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(UserFallWarn.this, "Improper", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(UserFallWarn.this, "Careful!", Toast.LENGTH_LONG).show();
                        mTTS.speak("I have detected a Fall. Click the button if you're injured or need help. We'll contact S O S if there is no activity for 3 minutes.", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callemergencynow();
            }
        },  180000);
    }

    public void callemergencyview(View view) {
        callemergencynow();
    }

    public void callemergencynow() {
        /*Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:9560188133"));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserFallWarn.this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    0);
            return;
        }
        startActivity(intent);*/
        SmsManager smsManager = SmsManager.getDefault();
        /*smsManager.sendTextMessage("+917703959795", null, "Anurag has been injured in a Fall.", null, null);*/
        Intent intent = new Intent(UserFallWarn.this,MainActivity.class);
        startActivity(intent);
    }

    public void cancelemergencyview(View view) {
        Intent intent = new Intent(UserFallWarn.this,MainActivity.class);
        startActivity(intent);
        mTTS.stop();
    }

}
