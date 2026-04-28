package com.example.resqlink_ai;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PreAlertActivity extends AppCompatActivity {

    TextView countdown;
    Button cancelBtn;
    CountDownTimer timer;
    int time = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_alert);

        countdown = findViewById(R.id.countdown);
        cancelBtn = findViewById(R.id.cancelBtn);

        // Countdown Timer
        timer = new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                countdown.setText(String.valueOf(time));
                time--;
            }

            @Override
            public void onFinish() {
                countdown.setText("0");

                // 🔥 Move to Alert Screen with data
                Intent intent = new Intent(PreAlertActivity.this, AlertActivity.class);
                intent.putExtra("type", getIntent().getStringExtra("type"));
                intent.putExtra("lat", getIntent().getDoubleExtra("lat", 0.0));
                intent.putExtra("lon", getIntent().getDoubleExtra("lon", 0.0));
                
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };

        timer.start();

        cancelBtn.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}