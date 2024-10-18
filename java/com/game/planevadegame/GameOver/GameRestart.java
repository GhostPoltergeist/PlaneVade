package com.game.planevadegame.GameOver;

import static com.game.planevadegame.GameStart.highScore;
import static com.game.planevadegame.animationFunction.zoomIn.applyZoomAnimation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.game.planevadegame.GameStart;
import com.game.planevadegame.MainActivity;
import com.game.planevadegame.R;

import org.w3c.dom.Text;

public class GameRestart extends AppCompatActivity implements View.OnClickListener {
    GameStart start = new GameStart();
    Button btnRestart;
    TextView scoring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_restart);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hiding Nav Bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        btnRestart = findViewById(R.id.btnMenu);
        btnRestart.setOnClickListener(this);

        scoring = findViewById(R.id.score);
        scoring.setText(String.valueOf(highScore));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnMenu) {
            applyZoomAnimation(v);
            restartGame();
        }
    }
    public void restartGame() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}