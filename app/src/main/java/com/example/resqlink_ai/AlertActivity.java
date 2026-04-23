package com.example.resqlink_ai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.*;
import android.media.MediaPlayer;
import android.os.*;
import android.provider.Settings;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AlertActivity extends AppCompatActivity {

    TextView locationText, countdownText, statusText;
    Button cancelBtn;

    LocationManager locationManager;
    CountDownTimer timer;
    MediaPlayer siren;

    boolean isCancelled = false;
    boolean isTriggered = false;

    String finalLink = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        locationText = findViewById(R.id.locationText);
        countdownText = findViewById(R.id.countdownText);
        statusText = findViewById(R.id.statusText);
        cancelBtn = findViewById(R.id.cancelBtn);

        // 🔊 Alarm sound (looping)
        siren = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        if (siren != null) {
            siren.setLooping(true);
            siren.start();
        }

        // ❌ Cancel button
        cancelBtn.setOnClickListener(v -> {
            isCancelled = true;
            countdownText.setText("Cancelled");
            statusText.setText("ALERT CANCELLED");

            if (timer != null) timer.cancel();
            if (siren != null && siren.isPlaying()) siren.stop();
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 📍 Permission check
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        getLocation();
    }

    // 📍 Get phone location
    private void getLocation() {

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                new LocationListener() {

                    @Override
                    public void onLocationChanged(@NonNull Location location) {

                        double lat = location.getLatitude();
                        double lon = location.getLongitude();

                        finalLink = "https://maps.google.com/?q=" + lat + "," + lon;

                        locationText.setText(finalLink);

                        if (!isTriggered && !isCancelled) {
                            startCountdown();
                            isTriggered = true;

                            // 🔥 Send to Firebase
                            sendToFirebase(finalLink);

                            locationManager.removeUpdates(this);
                        }
                    }

                    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override public void onProviderEnabled(@NonNull String provider) {}
                    @Override public void onProviderDisabled(@NonNull String provider) {}
                }
        );
    }

    // ⏳ Countdown before sending SMS
    private void startCountdown() {

        timer = new CountDownTimer(5000, 1000) {

            public void onTick(long ms) {
                countdownText.setText("Sending in: " + (ms / 1000));
            }

            public void onFinish() {
                if (!isCancelled) {
                    sendSMS();
                }
            }
        }.start();
    }

    // 📩 Auto SMS
    private void sendSMS() {

        try {
            android.telephony.SmsManager smsManager =
                    android.telephony.SmsManager.getDefault();

            String message = "🚨 EMERGENCY!\nLocation: " + finalLink;

            smsManager.sendTextMessage(
                    "9876543210",   // 🔥 REPLACE YOUR NUMBER
                    null,
                    message,
                    null,
                    null
            );

            statusText.setText("MESSAGE SENT ✔");

        } catch (Exception e) {
            statusText.setText("FAILED ❌");
            Toast.makeText(this, "SMS Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔥 Send data to Firebase
    private void sendToFirebase(String locationLink) {

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("alerts");

        HashMap<String, Object> data = new HashMap<>();

        data.put("type", "TEST");
        data.put("location", locationLink);
        data.put("time", System.currentTimeMillis());

        ref.push().setValue(data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (siren != null) {
            siren.release();
        }
    }
}