package com.example.resqlink_ai;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AlertActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView locationText, countdownText, typeText;
    Button cancelBtn;
    LinearLayout policeBtn, ambulanceBtn, momBtn, dadBtn, sisterBtn;

    CountDownTimer timer;
    MediaPlayer siren;
    GoogleMap mMap;

    boolean isCancelled = false;
    String finalLink = "Location pending...";
    
    private final String FIREBASE_URL = "https://resq-8f0d3-default-rtdb.asia-southeast1.firebasedatabase.app/";

    // Action for tracking SMS status
    private static final String SMS_SENT_ACTION = "SMS_SENT_ACTION";

    // Family Contacts
    String momNumber = "9108296410";
    String dadNumber = "9606077664";
    String sisterNumber = "9035858622";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        locationText = findViewById(R.id.locationText);
        countdownText = findViewById(R.id.countdownText);
        typeText = findViewById(R.id.typeText);
        cancelBtn = findViewById(R.id.cancelBtn);
        
        policeBtn = findViewById(R.id.policeBtn);
        ambulanceBtn = findViewById(R.id.ambulanceBtn);
        momBtn = findViewById(R.id.momBtn);
        dadBtn = findViewById(R.id.dadBtn);
        sisterBtn = findViewById(R.id.sisterBtn);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        String typeStr = getIntent().getStringExtra("type");
        double initLat = getIntent().getDoubleExtra("lat", 0.0);
        double initLon = getIntent().getDoubleExtra("lon", 0.0);

        if (typeStr != null) {
            typeText.setText(typeStr + " DETECTED");
        }
        
        if (initLat != 0.0 && initLon != 0.0) {
            finalLink = "https://maps.google.com/?q=" + initLat + "," + initLon;
            locationText.setText("📍 Location Loaded from Band");
        }

        policeBtn.setOnClickListener(v -> makeCall("100"));
        ambulanceBtn.setOnClickListener(v -> makeCall("102"));
        momBtn.setOnClickListener(v -> makeCall(momNumber));
        dadBtn.setOnClickListener(v -> makeCall(dadNumber));
        sisterBtn.setOnClickListener(v -> makeCall(sisterNumber));

        siren = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        if (siren != null) {
            siren.setLooping(true);
            siren.start();
        }

        listenToFirebase();
        startCountdown();

        cancelBtn.setOnClickListener(v -> {
            isCancelled = true;
            if (timer != null) timer.cancel();
            if (siren != null && siren.isPlaying()) siren.stop();
            finish(); 
        });

        // Register receiver for SMS status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsReceiver, new IntentFilter(SMS_SENT_ACTION), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(smsReceiver, new IntentFilter(SMS_SENT_ACTION));
        }
    }

    private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = "SMS Status: ";
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    status += "Sent Successfully ✔";
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    status += "Generic Failure (Check Balance/SIM) ❌";
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    status += "No Service ❌";
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    status += "Null PDU ❌";
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    status += "Radio Off ❌";
                    break;
                default:
                    status += "Failed (Code: " + getResultCode() + ") ❌";
                    break;
            }
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
            Log.d("SOS_DEBUG", status);
            
            runOnUiThread(() -> {
                if (getResultCode() == Activity.RESULT_OK) {
                    countdownText.setText("SOS Messages Sent ✔");
                } else {
                    countdownText.setText("SMS Failed ❌");
                }
            });
        }
    };

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        double initLat = getIntent().getDoubleExtra("lat", 0.0);
        double initLon = getIntent().getDoubleExtra("lon", 0.0);
        if (initLat != 0.0 && initLon != 0.0) {
            updateMap(initLat, initLon);
        }
    }

    private void updateMap(double lat, double lon) {
        if (mMap != null) {
            LatLng loc = new LatLng(lat, lon);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(loc).title("Emergency Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f));
        }
    }

    private void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    private void listenToFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("users/device_01/emergency");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isCancelled) {
                    try {
                        Object latObj = snapshot.child("latitude").getValue();
                        Object lonObj = snapshot.child("longitude").getValue();

                        if (latObj != null && lonObj != null) {
                            double lat = Double.parseDouble(latObj.toString());
                            double lon = Double.parseDouble(lonObj.toString());
                            finalLink = "https://maps.google.com/?q=" + lat + "," + lon;
                            locationText.setText("📍 Location Updated");
                            updateMap(lat, lon);
                        }
                    } catch (Exception e) {
                        Log.e("SOS_LOCATION", "Error: " + e.getMessage());
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startCountdown() {
        timer = new CountDownTimer(3000, 1000) {
            public void onTick(long ms) {
                countdownText.setText("Sending SOS in " + (ms/1000) + "s");
            }
            public void onFinish() {
                if (!isCancelled) {
                    sendSMSAutonomous();
                }
            }
        }.start();
    }

    private void sendSMSAutonomous() {
        // Final runtime check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            runOnUiThread(() -> Toast.makeText(this, "SMS Permission Denied!", Toast.LENGTH_LONG).show());
            return;
        }

        new Thread(() -> {
            try {
                Log.d("SOS_DEBUG", "sendSMSAutonomous() starting...");
                
                String type = getIntent().getStringExtra("type");
                if (type == null) type = "EMERGENCY";
                String message = "🚨 HELP NEEDED!\nCondition: " + type + "\nLocation: " + finalLink;

                // 🚀 The most compatible method for Xiaomi/Redmi (Android 14 / HyperOS)
                SmsManager smsManager;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    smsManager = this.getSystemService(SmsManager.class);
                } else {
                    smsManager = SmsManager.getDefault();
                }
                
                if (smsManager != null) {
                    String[] numbers = {momNumber, dadNumber, sisterNumber};
                    
                    for (String number : numbers) {
                        if (isCancelled) break;

                        // 1. Break long messages (especially with Emojis/Unicode which limit segments to 70 chars)
                        ArrayList<String> parts = smsManager.divideMessage(message);
                        
                        // 2. Setup PendingIntent to track delivery status
                        Intent intent = new Intent(SMS_SENT_ACTION);
                        // Using a unique requestCode for each number if needed, but here we just want to know if it's sent
                        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 
                                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                        
                        ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                        for (int i = 0; i < parts.size(); i++) {
                            sentIntents.add(pi);
                        }

                        // 3. Send Multi-part SMS to handle segments automatically
                        smsManager.sendMultipartTextMessage(number, null, parts, sentIntents, null);
                        
                        Thread.sleep(1200); // 1.2s pause to bypass hardware spam filters and ensure radio stability
                    }
                    
                    runOnUiThread(() -> {
                        countdownText.setText("SOS Dispatched...");
                    });
                    
                    Log.d("SOS_DEBUG", "All SMS triggered");
                }
            } catch (Exception e) {
                Log.e("SOS_ERROR", "SMS Failed: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(AlertActivity.this, "SMS Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (siren != null) siren.release();
        try {
            unregisterReceiver(smsReceiver);
        } catch (Exception ignored) {}
    }
}
