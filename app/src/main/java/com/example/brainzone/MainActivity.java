package com.example.brainzone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity – Main Menu / Home screen.
 *
 * Concepts demonstrated:
 *   • Activity lifecycle (Lecture 4)
 *   • SharedPreferences – read player name & high scores (Lecture 17)
 *   • Explicit Intents – navigate to each game / screen (Lecture 8)
 *   • Starting a Service – BackgroundMusicService (Lecture 15)
 *   • Stopping a Service – when app closes via onDestroy (Lecture 15)
 */
public class MainActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView tvWelcome;
    private TextView tvHighScore;
    private Button   btnGame1, btnGame2, btnGame3;
    private Button   btnLeaderboard, btnSettings;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ── Bind views (Lecture 6) ────────────────────────────────────────────
        tvWelcome      = findViewById(R.id.tvWelcome);
        tvHighScore    = findViewById(R.id.tvHighScore);
        btnGame1       = findViewById(R.id.btnGame1);
        btnGame2       = findViewById(R.id.btnGame2);
        btnGame3       = findViewById(R.id.btnGame3);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnSettings    = findViewById(R.id.btnSettings);

        // ── SharedPreferences (Lecture 17) ────────────────────────────────────
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // ── Start background music service (Lecture 15) ───────────────────────
        startMusicService();

        // ── Set up button click listeners (Lecture 6) ─────────────────────────
        btnGame1.setOnClickListener(v -> launchGame(Game1Activity.class, Constants.GAME_MULTIPLICATION));
        btnGame2.setOnClickListener(v -> launchGame(Game2Activity.class, Constants.GAME_MEMORY));
        btnGame3.setOnClickListener(v -> launchGame(Game3Activity.class, Constants.GAME_SCRAMBLE));

        btnLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI every time we return to this screen
        loadAndDisplayData();
    }

    /** Reads stored data and updates the welcome message and high score. */
    private void loadAndDisplayData() {
        String playerName = sharedPreferences.getString(Constants.KEY_PLAYER_NAME, "Player");
        tvWelcome.setText(getString(R.string.welcome_message, playerName));

        // All-time highest score across all games and difficulties
        int best = getAllTimeHighScore();
        tvHighScore.setText(getString(R.string.all_time_high, best));
    }

    /** Returns the single highest score saved across all games/difficulties. */
    private int getAllTimeHighScore() {
        int max = 0;
        String[] keys = {
            Constants.KEY_SCORE_MULT_EASY,   Constants.KEY_SCORE_MULT_MEDIUM, Constants.KEY_SCORE_MULT_HARD,
            Constants.KEY_SCORE_MEM_EASY,    Constants.KEY_SCORE_MEM_MEDIUM,  Constants.KEY_SCORE_MEM_HARD,
            Constants.KEY_SCORE_SCR_EASY,    Constants.KEY_SCORE_SCR_MEDIUM,  Constants.KEY_SCORE_SCR_HARD
        };
        for (String key : keys) {
            int val = sharedPreferences.getInt(key, 0);
            if (val > max) max = val;
        }
        return max;
    }

    /**
     * Launches a game Activity using an explicit Intent (Lecture 8).
     * Passes the game name and current difficulty as extras.
     */
    private void launchGame(Class<?> gameClass, String gameName) {
        String difficulty = sharedPreferences.getString(Constants.KEY_DIFFICULTY, Constants.DIFF_EASY);
        Intent intent = new Intent(MainActivity.this, gameClass);
        intent.putExtra(Constants.EXTRA_GAME_NAME,  gameName);    // putExtra (Lecture 8)
        intent.putExtra(Constants.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }

    /** Starts BackgroundMusicService (Lecture 15). */
    private void startMusicService() {
        Intent musicIntent = new Intent(this, BackgroundMusicService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(musicIntent);
        } else {
            startService(musicIntent);
        }
    }

    // ── Lifecycle (Lecture 4) ──────────────────────────────────────────────────
    @Override
    protected void onPause() { super.onPause(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop music service when the main activity is destroyed (Lecture 15)
        stopService(new Intent(this, BackgroundMusicService.class));
    }

    /** Confirm exit when back is pressed. */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_exit_title)
                .setMessage(R.string.dialog_exit_msg)
                .setPositiveButton(R.string.btn_yes, (d, w) -> {
                    stopService(new Intent(this, BackgroundMusicService.class));
                    finish();
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }
}
