package com.game.planevadegame.loadingScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.game.planevadegame.MainActivity;
import com.game.planevadegame.R;
import com.google.firebase.database.FirebaseDatabase;

public class planeVadeTitleScreen extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 second delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        setContentView(R.layout.activity_plane_vade);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the main activity
                Intent intent = new Intent(planeVadeTitleScreen.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the splash activity so it won't be shown when pressing back
            }
        }, SPLASH_DELAY);
    }
}