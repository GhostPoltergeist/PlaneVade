package com.game.planevadegame;

import static com.game.planevadegame.animationFunction.zoomIn.applyZoomAnimation;
import static com.game.planevadegame.questionandAnswer.wordlist.wordList.questionsWithAnswers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.game.planevadegame.GameOver.GameRestart;
import com.game.planevadegame.questionandAnswer.wordlist.wordList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class questions extends AppCompatActivity implements View.OnClickListener {
    private TextView answerOne, answerTwo, answerThree, answerFour, clock, questionTextView;
    private EditText finalAnswer;
    private Button exit, submit;
    private String correctAnswer;
    private CountDownTimer countDownTimer;
    private boolean isTransitioning = false; // Flag to track if we are transitionin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        // Load word list
        wordList.loadWordList();

        //TextView for clock
        clock = findViewById(R.id.timerTextView);

        //TextViews for correct answer
        answerOne = findViewById(R.id.answerOne);
        answerTwo = findViewById(R.id.answerTwo);
        answerThree = findViewById(R.id.answerThree);
        answerFour = findViewById(R.id.answerFour);
        questionTextView = findViewById(R.id.questionTextView);

        //EditText for final answer
        finalAnswer = findViewById(R.id.answerEditText);

        //Buttons
        exit = findViewById(R.id.btnExit);
        submit = findViewById(R.id.btnSubmit);

        //Exit and Submit button
        exit.setOnClickListener(this);
        submit.setOnClickListener(this);

        // Load a random question and answers
        loadRandomQuestion();
        startCountDownTimer();
    }

    @SuppressLint("SetTextI18n")
    private void loadRandomQuestion() {
        List<String> keys = new ArrayList<>(questionsWithAnswers.keySet());
        Collections.shuffle(keys);
        String randomQuestion = keys.get(0);
        correctAnswer = questionsWithAnswers.get(randomQuestion);

        questionTextView.setText(randomQuestion);

        List<String> answers = new ArrayList<>(questionsWithAnswers.values());
        answers.remove(correctAnswer);
        Collections.shuffle(answers);

        List<String> choices = new ArrayList<>();
        choices.add(correctAnswer);
        choices.add(answers.get(0));
        choices.add(answers.get(1));
        choices.add(answers.get(2));
        Collections.shuffle(choices);

        answerOne.setText("A. " + choices.get(0));
        answerTwo.setText("B. " + choices.get(1));
        answerThree.setText("C. " + choices.get(2));
        answerFour.setText("D. " + choices.get(3));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnExit) {
            applyZoomAnimation(view);
            finish();
        }

        if (id == R.id.btnSubmit) {
            applyZoomAnimation(view);
            checking();
        }
    }

    public void checking() {
        if (isTransitioning) {
            return; // Prevent further checks if already transitioning
        }

        String input = finalAnswer.getText().toString().trim();
        if (input.length() != 1) {
            failedGame();
            return;
        }

        char selectedAnswer = input.charAt(0);

        if (selectedAnswer != 'A' && selectedAnswer != 'a' &&
                selectedAnswer != 'B' && selectedAnswer != 'b' &&
                selectedAnswer != 'C' && selectedAnswer != 'c' &&
                selectedAnswer != 'D' && selectedAnswer != 'd') {
            failedGame();
            return;
        }

        boolean isCorrect = false;

        if (selectedAnswer == 'A' || selectedAnswer == 'a') {
            String gatheredAnswer = answerOne.getText().toString().substring(3);
            if (gatheredAnswer.equalsIgnoreCase(correctAnswer)) {
                isCorrect = true;
            }
        } else if (selectedAnswer == 'B' || selectedAnswer == 'b') {
            String gatheredAnswer = answerTwo.getText().toString().substring(3);
            if (gatheredAnswer.equalsIgnoreCase(correctAnswer)) {
                isCorrect = true;
            }
        } else if (selectedAnswer == 'C' || selectedAnswer == 'c') {
            String gatheredAnswer = answerThree.getText().toString().substring(3);
            if (gatheredAnswer.equalsIgnoreCase(correctAnswer)) {
                isCorrect = true;
            }
        } else if (selectedAnswer == 'D' || selectedAnswer == 'd') {
            String gatheredAnswer = answerFour.getText().toString().substring(3);
            if (gatheredAnswer.equalsIgnoreCase(correctAnswer)) {
                isCorrect = true;
            }
        }

        if (isCorrect) {
            restartGame();
        } else {
            failedGame();
        }
    }

    public void restartGame() {
        if (!isTransitioning) {
            isTransitioning = true;
            Intent intent = new Intent(this, GameStart.class);
            startActivity(intent);
            finish();
        }
    }

    public void failedGame() {
        if (!isTransitioning) {
            isTransitioning = true;
            Intent intent = new Intent(this, GameRestart.class);
            startActivity(intent);
            finish();
        }
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(20000, 1000) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                clock.setText("00:" + millisUntilFinished / 1000);

                int secondsRemaining = (int) (millisUntilFinished / 1000);
                int[] numbersToChangeText = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

                for (int number : numbersToChangeText) {
                    if (secondsRemaining == number) {
                        clock.setText("00:0" + millisUntilFinished / 1000 );
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                clock.setText("Time's up!");
                checking();
            }
        }.start();
    }
}
