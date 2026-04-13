package com.example.brainzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * GameResultActivity – shared results screen for all three games.
 *
 * Concepts:
 *   • getIntent().getStringExtra()  – read data from finishing game (Lecture 8)
 *   • Implicit Intent – ACTION_SEND to share score with other apps  (Lecture 9)
 *   • Explicit Intent – navigate back to Main Menu                  (Lecture 8)
 *   • SharedPreferences – read player name                          (Lecture 17)
 */
public class GameResultActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView tvTitle, tvGameName, tvScore, tvDifficulty,
                     tvExtra1Label, tvExtra1Value, tvRankMessage;
    private Button   btnPlayAgain, btnMainMenu, btnShare;

    private String gameName;
    private int    score;
    private String difficulty;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result);

        // ── Bind views ────────────────────────────────────────────────────────
        tvTitle       = findViewById(R.id.tvResultTitle);
        tvGameName    = findViewById(R.id.tvGameName);
        tvScore       = findViewById(R.id.tvFinalScore);
        tvDifficulty  = findViewById(R.id.tvDifficulty);
        tvExtra1Label = findViewById(R.id.tvExtra1Label);
        tvExtra1Value = findViewById(R.id.tvExtra1Value);
        tvRankMessage = findViewById(R.id.tvRankMessage);
        btnPlayAgain  = findViewById(R.id.btnPlayAgain);
        btnMainMenu   = findViewById(R.id.btnMainMenu);
        btnShare      = findViewById(R.id.btnShare);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // ── Read extras from the launching game activity (Lecture 8) ──────────
        gameName   = getIntent().getStringExtra(Constants.EXTRA_GAME_NAME);
        score      = getIntent().getIntExtra(Constants.EXTRA_SCORE, 0);
        difficulty = getIntent().getStringExtra(Constants.EXTRA_DIFFICULTY);
        if (difficulty == null) difficulty = Constants.DIFF_EASY;

        // ── Display results ───────────────────────────────────────────────────
        displayResults();

        // ── Button listeners ──────────────────────────────────────────────────

        // Play Again – re-launch the same game with same difficulty (Lecture 8)
        btnPlayAgain.setOnClickListener(v -> {
            Class<?> gameClass = getGameClass(gameName);
            if (gameClass != null) {
                Intent intent = new Intent(this, gameClass);
                intent.putExtra(Constants.EXTRA_GAME_NAME,  gameName);
                intent.putExtra(Constants.EXTRA_DIFFICULTY, difficulty);
                startActivity(intent);
                finish();
            }
        });

        // Back to Main Menu (Lecture 8 – explicit Intent)
        btnMainMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear game stack
            startActivity(intent);
            finish();
        });

        // Share score via Implicit Intent – ACTION_SEND (Lecture 9)
        btnShare.setOnClickListener(v -> shareScore());
    }

    /** Populates the result UI with the data received from the game. */
    private void displayResults() {
        String playerName = sharedPreferences.getString(Constants.KEY_PLAYER_NAME, "Player");

        tvTitle.setText(R.string.result_great_job);
        tvGameName.setText(gameName);
        tvScore.setText(getString(R.string.result_score, score));
        tvDifficulty.setText(getString(R.string.difficulty_label, difficulty.toUpperCase()));

        // Game-specific extras
        if (Constants.GAME_MEMORY.equals(gameName)) {
            int moves = getIntent().getIntExtra(Constants.EXTRA_MOVES, 0);
            int time  = getIntent().getIntExtra(Constants.EXTRA_TIME, 0);
            tvExtra1Label.setText(R.string.label_moves);
            tvExtra1Value.setText(getString(R.string.moves_time, moves, time));
        } else if (Constants.GAME_SCRAMBLE.equals(gameName)) {
            int accuracy = getIntent().getIntExtra(Constants.EXTRA_ACCURACY, 0);
            tvExtra1Label.setText(R.string.label_accuracy);
            tvExtra1Value.setText(getString(R.string.accuracy_value, accuracy));
        } else {
            tvExtra1Label.setText("");
            tvExtra1Value.setText("");
        }

        // Check if this is a new personal best
        int highScore = getHighScore();
        if (score >= highScore && score > 0) {
            tvRankMessage.setText(R.string.new_high_score);
        } else {
            tvRankMessage.setText(getString(R.string.best_label, highScore));
        }
    }

    /** Returns the stored high score for the current game + difficulty. */
    private int getHighScore() {
        String key = "";
        if (Constants.GAME_MULTIPLICATION.equals(gameName)) {
            switch (difficulty) {
                case Constants.DIFF_MEDIUM: key = Constants.KEY_SCORE_MULT_MEDIUM; break;
                case Constants.DIFF_HARD:   key = Constants.KEY_SCORE_MULT_HARD;   break;
                default:                    key = Constants.KEY_SCORE_MULT_EASY;   break;
            }
        } else if (Constants.GAME_MEMORY.equals(gameName)) {
            switch (difficulty) {
                case Constants.DIFF_MEDIUM: key = Constants.KEY_SCORE_MEM_MEDIUM; break;
                case Constants.DIFF_HARD:   key = Constants.KEY_SCORE_MEM_HARD;   break;
                default:                    key = Constants.KEY_SCORE_MEM_EASY;   break;
            }
        } else {
            switch (difficulty) {
                case Constants.DIFF_MEDIUM: key = Constants.KEY_SCORE_SCR_MEDIUM; break;
                case Constants.DIFF_HARD:   key = Constants.KEY_SCORE_SCR_HARD;   break;
                default:                    key = Constants.KEY_SCORE_SCR_EASY;   break;
            }
        }
        return key.isEmpty() ? 0 : sharedPreferences.getInt(key, 0);
    }

    /**
     * Shares the player's score via an Implicit Intent (Lecture 9).
     * ACTION_SEND lets the user pick any app that handles text sharing.
     */
    private void shareScore() {
        String playerName = sharedPreferences.getString(Constants.KEY_PLAYER_NAME, "Player");
        String shareText  = getString(R.string.share_text,
                playerName, score, gameName, difficulty.toUpperCase());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");          // MIME type (Lecture 9)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
    }

    /** Maps a game name string to its Activity class. */
    private Class<?> getGameClass(String name) {
        if (Constants.GAME_MULTIPLICATION.equals(name)) return Game1Activity.class;
        if (Constants.GAME_MEMORY.equals(name))         return Game2Activity.class;
        if (Constants.GAME_SCRAMBLE.equals(name))       return Game3Activity.class;
        return null;
    }

    // ── Lifecycle (Lecture 4) ──────────────────────────────────────────────────
    @Override protected void onPause()   { super.onPause();   }
    @Override protected void onResume()  { super.onResume();  }
    @Override protected void onDestroy() { super.onDestroy(); }

    /** Pressing back from Results goes to Main Menu. */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
