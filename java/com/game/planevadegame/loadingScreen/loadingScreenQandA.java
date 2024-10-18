package com.game.planevadegame.loadingScreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.game.planevadegame.MainActivity;
import com.game.planevadegame.R;
import com.game.planevadegame.questions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class loadingScreenQandA extends AppCompatActivity {
    TextView clock;
    private CountDownTimer countDownTimer;
    private TextView textView;
    private static final long SPLASH_DELAY = 5000; // 2 second delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen_qand);

        textView = findViewById(R.id.textView);
        clock = findViewById(R.id.countTextView);

        startCountDownTimer();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity
                Intent intent = new Intent(loadingScreenQandA.this, questions.class);
                startActivity(intent);
                finish(); // Close the splash activity so it won't be shown when pressing back
            }
        }, SPLASH_DELAY);
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(5000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                clock.setText("00:0" + millisUntilFinished / 1000);
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                clock.setText("Good-luck!");
                // Handle time up logic, such as disabling answer submission
            }
        }.start();
    }
}