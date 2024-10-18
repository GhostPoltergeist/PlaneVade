package com.game.planevadegame;

import static com.game.planevadegame.questionandAnswer.wordlist.wordList.loadWordList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.game.planevadegame.loadingScreen.loadingScreenQandA;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameStart extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    public static DatabaseReference mDatabase;
    // game scoring
    private int gameSpeed = 5;
    public static int highScore = 0;
    // game container
    RelativeLayout relativeContainer;
    // loop checks and breaks
    private boolean isShooting = false;
    private boolean collisionDetected = false;
    // intervals for shoot and missile generation:: can be change if {only if} needed
    private static long COOLDOWN_DURATION = 1000;
    private final int missileGenerationInterval = 3000;
    //handling runnable and handlers
    private final Handler handler = new Handler();
    private final Handler missileHandler = new Handler();
    private final Handler crateHandler = new Handler();
    private int generatePowerUpCrate = 40000;
    private int generateSpeedDecreaseCrate = 30000;
    private int generateBadLuckCrate = 50000;
    // in game music and sound effects
    public static MediaPlayer gameOver, shoot, music, collision, explode;
    // ImageView: jetPlane, missile, bullet image sources
    ImageView character_plane, missileImageView, bulletImageView, crateImageView, decreaseSpeedCrate, badluckCrate;
    // button for shooting projectiles
    Button shootBtn;
    // getting the jetplane location for X and Y
    ViewGroup.MarginLayoutParams layoutParams;
    // sensor manager for jetplane movement (declaration state)
    private Sensor accelerometerSensor;
    private float accelerationX = 0.0f;
    private final String playerName = "CurrentPlayer";
    private final String playerCurrentScore = "CurrentScore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_start);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // initializing game container
        relativeContainer = findViewById(R.id.game_layout);

        // Snowflake background animation
        ImageView imageView = findViewById(R.id.gifImageView);
        imageView.setAlpha(0.7f);

        //Sound Effect
        gameOver = MediaPlayer.create(this, R.raw.gameover);
        music = MediaPlayer.create(this, R.raw.dizzy);
        shoot = MediaPlayer.create(this, R.raw.laser);
        explode = MediaPlayer.create(this, R.raw.explode);
        collision = MediaPlayer.create(this, R.raw.collide);

        // Hiding Nav Bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        // adding click listener for shootBtn for projectiles
        shootBtn = findViewById(R.id.btnShoot);
        shootBtn.setOnClickListener(this);

        // getting the jetplane from resources
        character_plane = findViewById(R.id.character);

        // getting the jetplane coordinates
        layoutParams = (ViewGroup.MarginLayoutParams) character_plane.getLayoutParams();

        // sensor manager initialization (state)
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register this activity as a sensor listener
        if (accelerometerSensor != null)
        {sensorManager.registerListener((SensorEventListener) this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);}

        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadWordList();
        startMissileGeneration();
        startCratesGeneration();
        startSpeedDecreaseCrateGeneration();
        startBadLuckCrateGeneration();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometerSensor) {
            float x = event.values[0];
            // Adjust the jet-plane's movement based on accelerometer data
            accelerationX = x * 5;
            // Adjust the multiplier as needed
            updateJetplanePosition();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void updateJetplanePosition() {
        // Update jetplane position based on accelerometer data
        int newLeftMargin = layoutParams.leftMargin - (int) accelerationX;
        int maxWidth = getWindow().getDecorView().getWidth() - character_plane.getWidth();
        if (newLeftMargin >= 0 && newLeftMargin <= maxWidth) {
            layoutParams.leftMargin = newLeftMargin;
        }
        else if (newLeftMargin < 0) {
            layoutParams.leftMargin = 0;
        }
        else {
            layoutParams.leftMargin = maxWidth;
        }
        character_plane.setLayoutParams(layoutParams);
    }

    private void startMissileGeneration() {
        Runnable missileGenerationTask = new Runnable() {
            @Override
            public void run() {
                if (!collisionDetected) {
                    generateMissile(); // Generate a new missile
                }
                // Schedule the next missile generation in 2 seconds
                missileHandler.postDelayed(this, missileGenerationInterval);
            }
        };
        // Schedule the first missile generation after a delay
        missileHandler.postDelayed(missileGenerationTask, missileGenerationInterval);
    }

    private void generateMissile() {
            missileImageView = new ImageView(this);
            missileImageView.setImageResource(R.drawable.missile_fr); // Set the image resource

            // Set the size of the ImageView
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(120, 120);
            missileImageView.setLayoutParams(layoutParams);

            // Set center horizontal true
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

            // Get the width of the device's screen
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;

            // Set the position within the X=0 to X=(screenWidth - 100) range
            int xPosition = (int) (Math.random() * (screenWidth - 100));
            missileImageView.setX(xPosition);

            // Set the position within the Y=0 to Y=300 range
            int yPosition = (int) (Math.random() * 10); // Random value between 0 and 10
            missileImageView.setY(yPosition);

            // Add the missile ImageView to the container
            relativeContainer.addView(missileImageView);

            animateMissile(missileImageView);
            music.start();
    }

    private void animateMissile(final ImageView missileImageView) {
        final ViewGroup.MarginLayoutParams missile_layoutParams = (ViewGroup.MarginLayoutParams) missileImageView.getLayoutParams();

        Runnable msl_vade = new Runnable() {
            @Override
            public void run() {
                if (!collisionDetected) {
                    missile_layoutParams.topMargin += gameSpeed; // Move the missile down by 5 pixels (adjust as needed)
                    missileImageView.setLayoutParams(missile_layoutParams);
                    // Repeat the movement every X milliseconds
                    handler.postDelayed(this, 10); // Adjust the interval as needed

                    // Check for collision here, and set collisionDetected to true if a collision occurs

                    if (collisionOccurs(missileImageView, character_plane, 40)) {
                        collisionDetected = true;

                        music.stop();

                        explode.start();

                        gameOver.start();
                        gameOver();
                    }
                }
            }
        };
        handler.post(msl_vade);
        gameSpeed += 1;
    }

    private boolean collisionOccurs(ImageView missileImageView, ImageView characterImageView, int margin) {
        // Get the positions and sizes of the missile and character
        int missileX = (int) missileImageView.getX(); //45
        int missileY = (int) missileImageView.getY(); //100
        int missileWidth = missileImageView.getWidth(); //100
        int missileHeight = missileImageView.getHeight(); //100

        int characterX = (int) characterImageView.getX(); //20
        int characterY = (int) characterImageView.getY(); //fixed 40
        int characterWidth = characterImageView.getWidth(); //100
        int characterHeight = characterImageView.getHeight();//100

        // Adjust the character's hitbox by adding margin
        characterX += margin;
        characterY += margin;
        characterWidth -= 2 * margin;
        characterHeight -= 2 * margin;

        // Check for collision by comparing bounding boxes
        boolean collisionX = missileX + missileWidth >= characterX && missileX <= characterX + characterWidth;
        boolean collisionY = missileY + missileHeight >= characterY && missileY <= characterY + characterHeight;

        // Return true if there is a collision, false otherwise
        return collisionX && collisionY;
    }

    @Override
    public void onClick(View v) {
        int moveAmount = 13; // Adjust this bullet speed as needed

        if (v.getId() == R.id.btnShoot) {
            if (!isShooting) {
                generateBullet(character_plane); // Generate a new bullet
                shoot.start();
                isShooting = true;
                shootBtn.setEnabled(false);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isShooting = false;
                        shootBtn.setEnabled(true);
                    }
                }, COOLDOWN_DURATION);
            }
        }
    }

    private void generateBullet(ImageView jetplane_coordinates) {
        bulletImageView = new ImageView(this);
        bulletImageView.setImageResource(R.drawable.bullet_rf); // Set the image resource

        // Set the size of the ImageView
        LinearLayout.LayoutParams bulletlayoutParams = new LinearLayout.LayoutParams(60, 60);
        bulletImageView.setLayoutParams(bulletlayoutParams);

        // Set center horizontal true
        bulletlayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        int xPosition = (int) jetplane_coordinates.getX();
        bulletImageView.setX(xPosition);

        int yPosition = (int) jetplane_coordinates.getY();
        bulletImageView.setY(yPosition);

        // Add the bullet ImageView to the container
        relativeContainer.addView(bulletImageView);

        animateBullet(bulletImageView);
    }

    private int i = 0;
    private void animateBullet(final ImageView bulletImageView) {
        final ViewGroup.MarginLayoutParams bullet_layoutParams = (ViewGroup.MarginLayoutParams) bulletImageView.getLayoutParams();

        TextView score = findViewById(R.id.score);

        Runnable bullet_shoot = new Runnable() {
            private boolean collisionProcessed = false;

            @Override
            public void run() {
                if (!collisionDetected && !collisionProcessed) {
                    bullet_layoutParams.topMargin -= 15; // Move the bullet up by 15 pixels (adjust as needed)
                    bulletImageView.setLayoutParams(bullet_layoutParams);
                    // Repeat the movement every X milliseconds
                    handler.postDelayed(this, 20); // Adjust the interval as needed

                    if (missileImageView != null && collisionMissileAndBullet(missileImageView, bulletImageView, 50)) {
                        // Create an animation to fade out the missile image
                        AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
                        fadeOutAnimation.setDuration(100); // Duration of the animation in milliseconds

                        // Set the animation listener to remove the missile view after the animation
                        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                collision.start();
                                missileImageView.setImageResource(R.drawable.explosion);
                                relativeContainer.removeView(bulletImageView);
                                relativeContainer.removeView(missileImageView);
                                // Animation started
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                // Animation ended
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Animation repeated
                            }
                        });

                        // Start the fade out animation on the missile image
                        missileImageView.startAnimation(fadeOutAnimation);
                        bulletImageView.startAnimation(fadeOutAnimation);

                        // Update the score
                        i += 1;
                        score.setText(String.valueOf(i));

                        highScore = Integer.parseInt(String.valueOf(score.getText()));
                        saveCurrentScore(playerCurrentScore, highScore);
                        readHighScore(playerName, highScore);

                        // Mark the collision as processed to prevent further increments
                        collisionProcessed = true;
                    }
                }
            }
        };
        handler.post(bullet_shoot);
    }

    private boolean collisionMissileAndBullet(ImageView missileImageView, ImageView bulletImageView, int margin) {
        // Get the pos1itions and sizes of the missile and character
        int missileX = (int) missileImageView.getX();
        int missileY = (int) missileImageView.getY();
        int missileWidth = missileImageView.getWidth();
        int missileHeight = missileImageView.getHeight();

        int bulletX = (int) bulletImageView.getX();
        int bulletY = (int) bulletImageView.getY();
        int bulletWidth = bulletImageView.getWidth();
        int bulletHeight = bulletImageView.getHeight();

        // Adjust the bullet hitbox by adding margin
        bulletX += margin;
        bulletY += margin;
        bulletWidth -= 2 * margin;
        bulletHeight -= 2 * margin;

        // Check for collision by comparing bounding boxes
        boolean collisionX = missileX + missileWidth >= bulletX && missileX <= bulletX + bulletWidth;
        boolean collisionY = missileY + missileHeight >= bulletY && missileY <= bulletY + bulletHeight;

        // Return true if there is a collision, false otherwise
        return collisionX && collisionY;
    }

    private void startCratesGeneration() {
        Runnable crateGenerationTask = new Runnable() {
            @Override
            public void run() {
                if (!collisionDetected) {
                    generateCrates(); // Generate a new crate object
                }
                // Schedule the next crate generation in random seconds
                crateHandler.postDelayed(this, generatePowerUpCrate);
            }
        };
        // Schedule the first crate generation after a delay
        crateHandler.postDelayed(crateGenerationTask, generatePowerUpCrate);
    }

    private void generateCrates() {
        crateImageView = new ImageView(this);
        crateImageView.setImageResource(R.drawable.shootspeed); // Set the image resource

        // Set the size of the ImageView
        LinearLayout.LayoutParams cratelayoutParams = new LinearLayout.LayoutParams(100, 100);
        crateImageView.setLayoutParams(cratelayoutParams);

        // Set center horizontal true
        cratelayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        // Get the width of the device's screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Set the position within the X=0 to X=(screenWidth - 100) range
        int xPosition = (int) (Math.random() * (screenWidth - 100));
        crateImageView.setX(xPosition);

        // Set the position within the Y=0 to Y=300 range
        int yPosition = (int) (Math.random() * 10); // Random value between 0 and 10
        crateImageView.setY(yPosition);

        // Add the missile ImageView to the container
        relativeContainer.addView(crateImageView);

        animateCrate(crateImageView);
    }

    private void animateCrate(final ImageView crateImageView) {
        final ViewGroup.MarginLayoutParams crate_layoutParams = (ViewGroup.MarginLayoutParams) crateImageView.getLayoutParams();

        TextView crateDes = findViewById(R.id.crateDescription);

        Runnable power_up = new Runnable() {
            private boolean collisionProcessed = false;
            @Override
            public void run() {
                if (!collisionDetected && !collisionProcessed) {
                    crate_layoutParams.topMargin += 5; // Move the crate down by 5 pixels (adjust as needed)
                    crateImageView.setLayoutParams(crate_layoutParams);
                    // Repeat the movement every X milliseconds
                    handler.postDelayed(this, 10); // Adjust the interval as needed

                    // Check for collision here, and set collisionDetected to true if a collision occurs

                    if (collisionPowerCrateAndCharacter(crateImageView, character_plane, 40)) {
                        // Create an animation to fade out the missile image
                        AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
                        fadeOutAnimation.setDuration(100); // Duration of the animation in milliseconds

                        AlphaAnimation crateTextAnimation = new AlphaAnimation(1.0f, 0.0f);
                        crateTextAnimation.setDuration(2000); // Duration of the animation in milliseconds

                        // Set the animation listener to remove the missile view after the animation
                        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                crateImageView.setImageResource(R.drawable.obtain);
                                relativeContainer.removeView(crateImageView);
                                // Animation started
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                // Animation ended
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Animation repeated
                            }
                        });

                        crateImageView.startAnimation(fadeOutAnimation);
                        COOLDOWN_DURATION -= 100;

                        crateTextAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                crateDes.setText("+50 Shoot Speed");
                                // Animation started
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                crateDes.setText("");
                                // Animation ended
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Animation repeated
                            }
                        });

                        crateDes.startAnimation(crateTextAnimation);
                        collisionProcessed = true;
                    }
                }
            }
        };
        handler.post(power_up);
    }

    private boolean collisionPowerCrateAndCharacter(ImageView crateImageView, ImageView characterImageView, int margin) {
        // Get the pos1itions and sizes of the missile and character
        int crateX = (int) crateImageView.getX();
        int crateY = (int) crateImageView.getY();
        int crateWidth = crateImageView.getWidth();
        int crateHeight = crateImageView.getHeight();

        int characterX = (int) characterImageView.getX();
        int characterY = (int) characterImageView.getY();
        int characterWidth = characterImageView.getWidth();
        int characterHeight = characterImageView.getHeight();

        // Adjust the bullet hitbox by adding margin
        crateX += margin;
        crateY += margin;
        crateWidth -= 2 * margin;
        crateHeight -= 2 * margin;

        // Check for collision by comparing bounding boxes
        boolean collisionX = crateX + crateWidth >= characterX && crateX <= characterX + characterWidth;
        boolean collisionY = crateY + crateHeight >= characterY && crateY <= characterY + characterHeight;

        // Return true if there is a collision, false otherwise
        return collisionX && collisionY;
    }

    private void startSpeedDecreaseCrateGeneration() {
        Runnable SpeedDecreaseCrateGenerationTask = new Runnable() {
            @Override
            public void run() {
                if (!collisionDetected) {
                    generateSpeedDecreaseCrates(); // Generate a new crate object
                }
                // Schedule the next crate generation in random seconds
                crateHandler.postDelayed(this, generateSpeedDecreaseCrate);
            }
        };
        // Schedule the first crate generation after a delay
        crateHandler.postDelayed(SpeedDecreaseCrateGenerationTask, generateSpeedDecreaseCrate);
    }

    private void generateSpeedDecreaseCrates() {
        decreaseSpeedCrate = new ImageView(this);
        decreaseSpeedCrate.setImageResource(R.drawable.createcrossbrown); // Set the image resource

        // Set the size of the ImageView
        LinearLayout.LayoutParams decreaseSpeedlayoutParams = new LinearLayout.LayoutParams(100, 100);
        decreaseSpeedCrate.setLayoutParams(decreaseSpeedlayoutParams);

        // Set center horizontal true
        decreaseSpeedlayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        // Get the width of the device's screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Set the position within the X=0 to X=(screenWidth - 100) range
        int xPosition = (int) (Math.random() * (screenWidth - 100));
        decreaseSpeedCrate.setX(xPosition);

        // Set the position within the Y=0 to Y=300 range
        int yPosition = (int) (Math.random() * 10); // Random value between 0 and 10
        decreaseSpeedCrate.setY(yPosition);

        // Add the missile ImageView to the container
        relativeContainer.addView(decreaseSpeedCrate);

        animateDecreaseSpeedCrate(decreaseSpeedCrate);
    }
    private void animateDecreaseSpeedCrate(final ImageView decreaseSpeedCrate) {
        final ViewGroup.MarginLayoutParams crate_layoutParams = (ViewGroup.MarginLayoutParams) decreaseSpeedCrate.getLayoutParams();

        TextView crateDes = findViewById(R.id.crateDescription);

        Runnable power_up = new Runnable() {
            private boolean collisionProcessed = false;
            @Override
            public void run() {
                if (!collisionDetected && !collisionProcessed) {
                    crate_layoutParams.topMargin += 5; // Move the crate down by 5 pixels (adjust as needed)
                    decreaseSpeedCrate.setLayoutParams(crate_layoutParams);
                    // Repeat the movement every X milliseconds
                    handler.postDelayed(this, 10); // Adjust the interval as needed

                    // Check for collision here, and set collisionDetected to true if a collision occurs

                    if (collisionDecreaseSpeedCrateAndCharacter(decreaseSpeedCrate, character_plane, 40)) {
                        // Create an animation to fade out the missile image
                        AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
                        fadeOutAnimation.setDuration(100); // Duration of the animation in milliseconds

                        AlphaAnimation crateTextAnimation = new AlphaAnimation(1.0f, 0.0f);
                        crateTextAnimation.setDuration(2000); // Duration of the animation in milliseconds

                        // Set the animation listener to remove the missile view after the animation
                        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                decreaseSpeedCrate.setImageResource(R.drawable.obtain);
                                relativeContainer.removeView(decreaseSpeedCrate);
                                // Animation started
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                // Animation ended
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Animation repeated
                            }
                        });

                        decreaseSpeedCrate.startAnimation(fadeOutAnimation);
                        gameSpeed -= 10;

                        crateTextAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                crateDes.setText("-10 Missile Speed");
                                // Animation started
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                crateDes.setText("");
                                // Animation ended
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Animation repeated
                            }
                        });

                        crateDes.startAnimation(crateTextAnimation);
                        collisionProcessed = true;
                    }
                }
            }
        };
        handler.post(power_up);
    }

    private boolean collisionDecreaseSpeedCrateAndCharacter(ImageView decreaseSpeedCrateImageView, ImageView characterImageView, int margin) {
        // Get the pos1itions and sizes of the missile and character
        int decreaseSpeedCrateX = (int) decreaseSpeedCrateImageView.getX();
        int decreaseSpeedCrateY = (int) decreaseSpeedCrateImageView.getY();
        int decreaseSpeedCrateWidth = decreaseSpeedCrateImageView.getWidth();
        int decreaseSpeedCrateHeight = decreaseSpeedCrateImageView.getHeight();

        int characterX = (int) characterImageView.getX();
        int characterY = (int) characterImageView.getY();
        int characterWidth = characterImageView.getWidth();
        int characterHeight = characterImageView.getHeight();

        // Adjust the bullet hitbox by adding margin
        decreaseSpeedCrateX += margin;
        decreaseSpeedCrateY += margin;
        decreaseSpeedCrateWidth -= 2 * margin;
        decreaseSpeedCrateHeight -= 2 * margin;

        // Check for collision by comparing bounding boxes
        boolean collisionX = decreaseSpeedCrateX + decreaseSpeedCrateWidth >= characterX && decreaseSpeedCrateX <= characterX + characterWidth;
        boolean collisionY = decreaseSpeedCrateY + decreaseSpeedCrateHeight >= characterY && decreaseSpeedCrateY <= characterY + characterHeight;

        // Return true if there is a collision, false otherwise
        return collisionX && collisionY;
    }

    private void startBadLuckCrateGeneration() {
        Runnable badluckCrateGenerationTask = new Runnable() {
            @Override
            public void run() {
                if (!collisionDetected) {
                    generateBadLuckCrates(); // Generate a new crate object
                }
                // Schedule the next crate generation in random seconds
                crateHandler.postDelayed(this, generateBadLuckCrate);
            }
        };
        // Schedule the first crate generation after a delay
        crateHandler.postDelayed(badluckCrateGenerationTask, generateBadLuckCrate);
    }

    private void generateBadLuckCrates() {
        badluckCrate = new ImageView(this);
        badluckCrate.setImageResource(R.drawable.random); // Set the image resource

        // Set the size of the ImageView
        LinearLayout.LayoutParams badluckCratelayoutParams = new LinearLayout.LayoutParams(100, 100);
        badluckCrate.setLayoutParams(badluckCratelayoutParams);

        // Set center horizontal true
        badluckCratelayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        // Get the width of the device's screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // Set the position within the X=0 to X=(screenWidth - 100) range
        int xPosition = (int) (Math.random() * (screenWidth - 100));
        badluckCrate.setX(xPosition);

        // Set the position within the Y=0 to Y=300 range
        int yPosition = (int) (Math.random() * 10); // Random value between 0 and 10
        badluckCrate.setY(yPosition);

        // Add the missile ImageView to the container
        relativeContainer.addView(badluckCrate);

        animatebadluckCrate(badluckCrate);
    }
    private void animatebadluckCrate(final ImageView badluckCrate) {
        final ViewGroup.MarginLayoutParams crate_layoutParams = (ViewGroup.MarginLayoutParams) badluckCrate.getLayoutParams();

        TextView crateDes = findViewById(R.id.crateDescription);
        Context context = this;

        Runnable power_up = new Runnable() {
            private boolean collisionProcessed = false;

            @Override
            public void run() {
                if (!collisionDetected && !collisionProcessed) {
                    crate_layoutParams.topMargin += 5; // Move the crate down by 5 pixels (adjust as needed)
                    badluckCrate.setLayoutParams(crate_layoutParams);
                    // Repeat the movement every X milliseconds
                    handler.postDelayed(this, 10); // Adjust the interval as needed

                    // Check for collision here, and set collisionDetected to true if a collision occurs

                    if (collisionbadluckCrateAndCharacter(badluckCrate, character_plane, 40)) {
                        // Create an animation to fade out the missile image
                        AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
                        fadeOutAnimation.setDuration(100); // Duration of the animation in milliseconds

                        AlphaAnimation crateTextAnimation = new AlphaAnimation(1.0f, 0.0f);
                        crateTextAnimation.setDuration(2000); // Duration of the animation in milliseconds

                        // Set the animation listener to remove the missile view after the animation
                        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                badluckCrate.setImageResource(R.drawable.obtain);
                                relativeContainer.removeView(badluckCrate);
                                // Animation started
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                // Animation ended
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Animation repeated
                            }
                        });

                        badluckCrate.startAnimation(fadeOutAnimation);
                        gameSpeed += 10;

                        crateTextAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                crateDes.setTextColor(ContextCompat.getColor(context, R.color.red));
                                crateDes.setText("BadLuck!");
                                // Animation started
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                crateDes.setText("");
                                // Animation ended
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Animation repeated
                            }
                        });

                        crateDes.startAnimation(crateTextAnimation);
                        collisionProcessed = true;
                    }
                }
            }
        };
        handler.post(power_up);
    }
    private boolean collisionbadluckCrateAndCharacter(ImageView badluckCrateImageView, ImageView characterImageView, int margin) {
        // Get the pos1itions and sizes of the missile and character
        int badluckCrateX = (int) badluckCrateImageView.getX();
        int badluckCrateY = (int) badluckCrateImageView.getY();
        int badluckCrateWidth = badluckCrateImageView.getWidth();
        int badluckCrateHeight = badluckCrateImageView.getHeight();

        int characterX = (int) characterImageView.getX();
        int characterY = (int) characterImageView.getY();
        int characterWidth = characterImageView.getWidth();
        int characterHeight = characterImageView.getHeight();

        // Adjust the bullet hitbox by adding margin
        badluckCrateX += margin;
        badluckCrateY += margin;
        badluckCrateWidth -= 2 * margin;
        badluckCrateHeight -= 2 * margin;

        // Check for collision by comparing bounding boxes
        boolean collisionX = badluckCrateX + badluckCrateWidth >= characterX && badluckCrateX <= characterX + characterWidth;
        boolean collisionY = badluckCrateY + badluckCrateHeight >= characterY && badluckCrateY <= characterY + characterHeight;

        // Return true if there is a collision, false otherwise
        return collisionX && collisionY;
    }
    public void gameOver() {
        Intent intent = new Intent(this, loadingScreenQandA.class);
        startActivity(intent);
        finish();
    }
    private void saveHighScore(String playerName, int highScore) {
        mDatabase.child("HighScore").child(playerName).setValue(highScore);
    }
    private void saveCurrentScore(String playerName, int highScore) {
        mDatabase.child("CurrentScore").child(playerName).setValue(highScore);
    }
    private void readHighScore(String playerName, int localHighScore) {
        mDatabase.child("HighScore").child(playerName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                Integer onlineHighScore = dataSnapshot.getValue(Integer.class);
                if (onlineHighScore != null) {
                    Log.d("MainActivity", "Online high score: " + onlineHighScore);
                    // Compare the retrieved high score with the local high score
                    if (localHighScore > onlineHighScore) {
                        saveHighScore(playerName, highScore);
                    } else {
                        Log.d("MainActivity", "Online high score is higher or equal. No update needed.");
                    }
                } else {
                    saveHighScore(playerName, localHighScore);
                }
            } else {
                Log.w("MainActivity", "Error getting data", task.getException());
            }
        });
    }
}