package com.game.planevadegame;

import static com.game.planevadegame.animationFunction.zoomIn.applyZoomAnimation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.game.planevadegame.AboutDev.About;
import com.game.planevadegame.developer.devs;
import com.game.planevadegame.support.supportUs;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart, btnAbout;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(this);

        // Hiding Nav Bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        drawerLayout = findViewById(R.id.title_drawer_layout);

        NavigationView navigationView = findViewById(R.id.title_nav_view);

        MenuItem navPlaneItem = navigationView.getMenu().findItem(R.id.nav_plane);
        MenuItem navDevItem = navigationView.getMenu().findItem(R.id.nav_dev);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == navPlaneItem.getItemId()) {
                    Intent intent = new Intent(getApplicationContext(), supportUs.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                if (item.getItemId() == navDevItem.getItemId()) {
                    Intent intent = new Intent(getApplicationContext(), devs.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnStart) {
            applyZoomAnimation(v);
            openGame();
        }
        else if (v.getId() == R.id.btnAbout) {
            applyZoomAnimation(v);
            openAbout();
        }
    }

    public void openGame() {
        Intent intent = new Intent(this, GameStart.class);
        startActivity(intent);
        finish();
    }
    public void openAbout() {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
        finish();
    }

}