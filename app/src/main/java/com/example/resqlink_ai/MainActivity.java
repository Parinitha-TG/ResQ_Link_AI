package com.example.resqlink_ai;

import android.content.Intent;
import android.os.*;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button sosBtn;
    TextView statusText;

    Handler handler = new Handler();
    boolean isHolding = false;
    int seconds = 3;

    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sosBtn = findViewById(R.id.sosBtn);
        statusText = findViewById(R.id.statusText);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

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

        if (vibrator != null) {
            vibrator.vibrate(200);
        }
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
                triggerSOS();
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void triggerSOS() {
        Intent i = new Intent(MainActivity.this, AlertActivity.class);
        i.putExtra("type", "SOS");
        startActivity(i);
    }
}