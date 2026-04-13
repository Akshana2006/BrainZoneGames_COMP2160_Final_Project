package com.example.brainzone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Game1Activity – Multiplication Puzzle.
 *
 * Concepts demonstrated:
 *   • Activity lifecycle with onPause / onResume / onDestroy (Lecture 4)
 *   • onSaveInstanceState / onRestoreInstanceState   (Lecture 4)
 *   • CountDownTimer for per-question timing
 *   • SharedPreferences – save high score            (Lecture 17)
 *   • BroadcastReceiver – ScreenOffReceiver          (Lecture 18)
 *   • Explicit Intent   – go to GameResultActivity   (Lecture 8)
 *   • Custom broadcast  – ACTION_GAME_OVER           (Lecture 18)
 *
 * Scoring: +10 correct | 0 wrong | -5 timeout
 */
public class Game1Activity extends AppCompatActivity implements ScreenOffReceiver.GamePauseListener {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView  tvQuestion, tvScore, tvQuestionNum, tvTimer, tvStreak;
    private EditText  etAnswer;
    private Button    btnSubmit, btnClear;
    private ProgressBar pbTimer;

    // ── Game state ────────────────────────────────────────────────────────────
    private int      totalScore   = 0;
    private int      questionNum  = 0;
    private int      streak       = 0;
    private int      correctAnswer;
    private boolean  gamePaused   = false;
    private boolean  timerRunning = false;

    private static final int TOTAL_QUESTIONS = 10;
    private static final int POINTS_CORRECT  = 10;
    private static final int POINTS_TIMEOUT  = -5;

    // ── Difficulty ────────────────────────────────────────────────────────────
    private String   difficulty;
    private int      timerSeconds; // per question

    // ── Duplicate-prevention list ─────────────────────────────────────────────
    private final List<String> usedEquations = new ArrayList<>();

    // ── Timer & BroadcastReceiver ─────────────────────────────────────────────
    private CountDownTimer countDownTimer;
    private ScreenOffReceiver screenOffReceiver;

    private SharedPreferences sharedPreferences;
    private Random random = new Random();

    // ── Keys for onSaveInstanceState ─────────────────────────────────────────
    private static final String STATE_SCORE    = "state_score";
    private static final String STATE_QNUM     = "state_question_num";
    private static final String STATE_STREAK   = "state_streak";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game1);

        // ── Bind views ────────────────────────────────────────────────────────
        tvQuestion   = findViewById(R.id.tvQuestion);
        tvScore      = findViewById(R.id.tvScore);
        tvQuestionNum= findViewById(R.id.tvQuestionNum);
        tvTimer      = findViewById(R.id.tvTimer);
        tvStreak     = findViewById(R.id.tvStreak);
        etAnswer     = findViewById(R.id.etAnswer);
        btnSubmit    = findViewById(R.id.btnSubmit);
        btnClear     = findViewById(R.id.btnClear);
        pbTimer      = findViewById(R.id.pbTimer);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // ── Read intent extras (Lecture 8) ────────────────────────────────────
        difficulty = getIntent().getStringExtra(Constants.EXTRA_DIFFICULTY);
        if (difficulty == null) difficulty = Constants.DIFF_EASY;

        // Set timer length by difficulty
        switch (difficulty) {
            case Constants.DIFF_HARD:   timerSeconds = 7;  break;
            case Constants.DIFF_MEDIUM: timerSeconds = 12; break;
            default:                    timerSeconds = 20; break;
        }

        // ── Restore state after rotation (Lecture 4) ──────────────────────────
        if (savedInstanceState != null) {
            totalScore  = savedInstanceState.getInt(STATE_SCORE,  0);
            questionNum = savedInstanceState.getInt(STATE_QNUM,   0);
            streak      = savedInstanceState.getInt(STATE_STREAK, 0);
        }

        // ── Button listeners ──────────────────────────────────────────────────
        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnClear.setOnClickListener(v  -> etAnswer.setText(""));

        // ── BroadcastReceiver setup (Lecture 18) ──────────────────────────────
        screenOffReceiver = new ScreenOffReceiver(this);

        // ── Start the first question ──────────────────────────────────────────
        loadNextQuestion();
    }

    // ── ScreenOffReceiver.GamePauseListener ───────────────────────────────────
    @Override
    public void onScreenOff() {
        pauseGame();
    }

    // ── Question logic ────────────────────────────────────────────────────────

    /** Generates and displays a new question. */
    private void loadNextQuestion() {
        if (questionNum >= TOTAL_QUESTIONS) {
            endGame();
            return;
        }

        questionNum++;
        tvQuestionNum.setText(getString(R.string.question_num, questionNum, TOTAL_QUESTIONS));
        tvScore.setText(getString(R.string.score_label, totalScore));
        tvStreak.setText(getString(R.string.streak_label, streak));
        etAnswer.setText("");

        generateEquation();
        startTimer();
    }

    /**
     * Generates a multiplication (or division for Medium/Hard) equation
     * with one missing value.  Avoids repeating the same equation.
     */
    private void generateEquation() {
        int maxNum;
        switch (difficulty) {
            case Constants.DIFF_HARD:   maxNum = 50; break;
            case Constants.DIFF_MEDIUM: maxNum = 20; break;
            default:                    maxNum = 10; break;
        }

        String equation;
        int    answer;
        int    attempts = 0;

        do {
            int a = random.nextInt(maxNum) + 1;
            int b = random.nextInt(maxNum) + 1;
            int c = a * b;

            // For Medium and Hard, sometimes use division
            boolean useDivision = (difficulty.equals(Constants.DIFF_MEDIUM)
                                || difficulty.equals(Constants.DIFF_HARD))
                                && random.nextBoolean();

            if (useDivision) {
                // a ÷ b = c  →  a is already b*c; hide one of the three slots
                int slot = random.nextInt(3);
                if (slot == 0) {
                    // ? ÷ b = c
                    equation = "? ÷ " + b + " = " + c;
                    answer   = a;          // a = b * c
                } else if (slot == 1) {
                    // a ÷ ? = c
                    equation = a + " ÷ ? = " + c;
                    answer   = b;
                } else {
                    // a ÷ b = ?
                    equation = a + " ÷ " + b + " = ?";
                    answer   = c;
                }
            } else {
                // Multiplication – hide one slot
                int slot = random.nextInt(3);
                if (slot == 0) {
                    equation = "? × " + b + " = " + c;
                    answer   = a;
                } else if (slot == 1) {
                    equation = a + " × ? = " + c;
                    answer   = b;
                } else {
                    equation = a + " × " + b + " = ?";
                    answer   = c;
                }
            }
            attempts++;
        } while (usedEquations.contains(equation) && attempts < 50);

        usedEquations.add(equation);
        correctAnswer = answer;
        tvQuestion.setText(equation);
    }

    /** Starts a per-question countdown. */
    private void startTimer() {
        timerRunning = true;
        pbTimer.setMax(timerSeconds);
        pbTimer.setProgress(timerSeconds);

        countDownTimer = new CountDownTimer(timerSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText(getString(R.string.timer_label, secondsLeft));
                pbTimer.setProgress(secondsLeft);
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                // Time expired
                totalScore += POINTS_TIMEOUT;
                if (totalScore < 0) totalScore = 0;
                streak = 0;
                Toast.makeText(Game1Activity.this,
                        getString(R.string.time_up, correctAnswer), Toast.LENGTH_SHORT).show();
                loadNextQuestion();
            }
        }.start();
    }

    /** Called when the player presses Submit. */
    private void checkAnswer() {
        if (!timerRunning) return;

        String input = etAnswer.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, R.string.enter_answer, Toast.LENGTH_SHORT).show();
            return;
        }

        cancelTimer();

        int playerAnswer;
        try {
            playerAnswer = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
            return;
        }

        if (playerAnswer == correctAnswer) {
            totalScore += POINTS_CORRECT;
            streak++;
            Toast.makeText(this, getString(R.string.correct, totalScore), Toast.LENGTH_SHORT).show();
        } else {
            streak = 0;
            Toast.makeText(this,
                    getString(R.string.wrong_answer, correctAnswer), Toast.LENGTH_SHORT).show();
        }

        loadNextQuestion();
    }

    /** Stops the active countdown. */
    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        timerRunning = false;
    }

    /** Pauses the game (e.g., screen off). */
    private void pauseGame() {
        if (!gamePaused) {
            gamePaused = true;
            cancelTimer();
            Toast.makeText(this, R.string.game_paused, Toast.LENGTH_SHORT).show();
        }
    }

    /** Called after all 10 questions are answered. */
    private void endGame() {
        cancelTimer();
        saveHighScore();
        sendGameOverBroadcast();
        goToResults();
    }

    // ── SharedPreferences – save high score (Lecture 17) ─────────────────────
    private void saveHighScore() {
        String key;
        switch (difficulty) {
            case Constants.DIFF_MEDIUM: key = Constants.KEY_SCORE_MULT_MEDIUM; break;
            case Constants.DIFF_HARD:   key = Constants.KEY_SCORE_MULT_HARD;   break;
            default:                    key = Constants.KEY_SCORE_MULT_EASY;   break;
        }
        int previous = sharedPreferences.getInt(key, 0);
        if (totalScore > previous) {
            sharedPreferences.edit().putInt(key, totalScore).apply();
        }

        // Save to leaderboard top-5
        String playerName = sharedPreferences.getString(Constants.KEY_PLAYER_NAME, "Player");
        LeaderboardActivity.saveScore(sharedPreferences,
                Constants.LB_MULT_PREFIX, playerName, totalScore, difficulty);
    }

    // ── Custom BroadcastReceiver – game over broadcast (Lecture 18) ───────────
    private void sendGameOverBroadcast() {
        Intent broadcastIntent = new Intent(Constants.ACTION_GAME_OVER);
        broadcastIntent.putExtra(Constants.EXTRA_GAME_NAME, Constants.GAME_MULTIPLICATION);
        broadcastIntent.putExtra(Constants.EXTRA_SCORE, totalScore);
        sendBroadcast(broadcastIntent);
    }

    /** Navigates to GameResultActivity with explicit Intent (Lecture 8). */
    private void goToResults() {
        Intent intent = new Intent(this, GameResultActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_NAME,  Constants.GAME_MULTIPLICATION);
        intent.putExtra(Constants.EXTRA_SCORE,      totalScore);
        intent.putExtra(Constants.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
        finish();
    }

    // ── onSaveInstanceState – preserve score across rotation (Lecture 4) ──────
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SCORE,  totalScore);
        outState.putInt(STATE_QNUM,   questionNum);
        outState.putInt(STATE_STREAK, streak);
    }

    // ── Lifecycle (Lecture 4) ──────────────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        // Register dynamic BroadcastReceiver (Lecture 18)
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenOffReceiver, filter);
        gamePaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        // Unregister dynamic BroadcastReceiver (Lecture 18)
        try { unregisterReceiver(screenOffReceiver); } catch (Exception ignored) {}
    }

    /** Confirm exit when back is pressed during an active game. */
    @Override
    public void onBackPressed() {
        cancelTimer();
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_quit_title)
                .setMessage(R.string.dialog_quit_msg)
                .setPositiveButton(R.string.btn_yes, (d, w) -> finish())
                .setNegativeButton(R.string.btn_no, (d, w) -> {
                    if (!gamePaused) loadNextQuestion();
                })
                .show();
    }
}
