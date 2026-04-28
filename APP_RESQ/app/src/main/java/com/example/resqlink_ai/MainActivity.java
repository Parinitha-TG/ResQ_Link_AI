package com.example.resqlink_ai;

import android.content.Intent;
import android.os.*;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Button sosBtn;
    TextView statusText, deviceStatusText;

    Handler handler = new Handler();
    boolean isHolding = false;
    int seconds = 3;

    // Use specific URL from user screenshot to ensure connection
    private final String FIREBASE_URL = "https://resq-8f0d3-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sosBtn = findViewById(R.id.sosBtn);
        statusText = findViewById(R.id.statusText);
        deviceStatusText = findViewById(R.id.deviceStatusText);

        // Listen to Firebase for triggers
        listenToBand();

        // Request SMS permission early
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 101);
            }
        }

        sosBtn.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startHold();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopHold();
                    break;
            }
            return true;
        });
    }

    private void startHold() {
        isHolding = true;
        seconds = 3;
        handler.postDelayed(runnable, 1000);
    }

    private void stopHold() {
        isHolding = false;
        handler.removeCallbacks(runnable);
        statusText.setText("Press and hold for 3 seconds");
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isHolding) return;
            seconds--;
            statusText.setText("Hold... " + seconds);
            if (seconds == 0) {
                triggerSOS("MANUAL SOS", 0.0, 0.0); // Manual SOS might not have GPS yet
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void triggerSOS(String type, double lat, double lon) {
        Intent i = new Intent(MainActivity.this, PreAlertActivity.class);
        i.putExtra("type", type);
        i.putExtra("lat", lat);
        i.putExtra("lon", lon);
        startActivity(i);
    }

    private void listenToBand() {
        DatabaseReference ref = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("users/device_01/emergency");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String event = snapshot.child("event").getValue(String.class);
                    
                    // Also read location here to pass it forward
                    Object latObj = snapshot.child("latitude").getValue();
                    Object lonObj = snapshot.child("longitude").getValue();
                    
                    double lat = 0.0;
                    double lon = 0.0;
                    
                    if (latObj != null) lat = Double.parseDouble(latObj.toString());
                    if (lonObj != null) lon = Double.parseDouble(lonObj.toString());

                    if (event != null) {
                        if (deviceStatusText != null) {
                            deviceStatusText.setText("Band Status: " + event);
                            deviceStatusText.setTextColor(event.equalsIgnoreCase("NORMAL") ? 0xFF4CAF50 : 0xFFF44336);
                        }

                        if (event.equalsIgnoreCase("FALL") || event.equalsIgnoreCase("DISTRESS")) {
                            triggerSOS(event, lat, lon);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}