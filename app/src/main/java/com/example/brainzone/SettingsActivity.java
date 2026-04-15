package com.example.brainzone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SettingsActivity – controls music, SFX, difficulty and data reset.
 *
 * ALL settings are persisted using SharedPreferences (Lecture 17):
 *   music_enabled  – Boolean
 *   sfx_enabled    – Boolean
 *   difficulty     – String ("easy" | "medium" | "hard")
 *
 * Views used:
 *   ToggleButton  – music and SFX toggles    (Lecture 7)
 *   RadioGroup    – difficulty selection     (Lecture 7)
 *   Button        – reset and back           (Lecture 6)
 */
public class SettingsActivity extends AppCompatActivity {

    private ToggleButton tbMusic, tbSFX;
    private RadioGroup   rgDifficulty;
    private RadioButton  rbEasy, rbMedium, rbHard;
    private Button       btnResetScores, btnBack;

    private SharedPreferences      sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // ── Bind views ────────────────────────────────────────────────────────
        tbMusic       = findViewById(R.id.tbMusic);
        tbSFX         = findViewById(R.id.tbSFX);
        rgDifficulty  = findViewById(R.id.rgDifficulty);
        rbEasy        = findViewById(R.id.rbEasy);
        rbMedium      = findViewById(R.id.rbMedium);
        rbHard        = findViewById(R.id.rbHard);
        btnResetScores= findViewById(R.id.btnResetScores);
        btnBack       = findViewById(R.id.btnBack);

        // ── Load saved settings (Lecture 17 – read) ───────────────────────────
        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        editor            = sharedPreferences.edit();
        loadSettings();

        // ── Listeners ─────────────────────────────────────────────────────────

        // Music toggle (Lecture 7 – ToggleButton)
        tbMusic.setOnCheckedChangeListener((btn, isChecked) -> {
            editor.putBoolean(Constants.KEY_MUSIC_ENABLED, isChecked).apply();
            // Restart / stop music service to reflect the change
            Intent musicIntent = new Intent(this, BackgroundMusicService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(musicIntent);
            } else {
                startService(musicIntent);
            }
        });

        // SFX toggle
        tbSFX.setOnCheckedChangeListener((btn, isChecked) ->
                editor.putBoolean(Constants.KEY_SFX_ENABLED, isChecked).apply());

        // Difficulty RadioGroup (Lecture 7 – RadioButton)
        rgDifficulty.setOnCheckedChangeListener((group, checkedId) -> {
            String diff;
            if (checkedId == R.id.rbMedium)      diff = Constants.DIFF_MEDIUM;
            else if (checkedId == R.id.rbHard)   diff = Constants.DIFF_HARD;
            else                                  diff = Constants.DIFF_EASY;
            editor.putString(Constants.KEY_DIFFICULTY, diff).apply();
            Toast.makeText(this,
                    getString(R.string.difficulty_set, diff.toUpperCase()),
                    Toast.LENGTH_SHORT).show();
        });

        // Reset all scores with a confirmation dialog (Lecture 6 – AlertDialog)
        btnResetScores.setOnClickListener(v -> showResetDialog());

        // Back button – return to MainActivity (Lecture 8 – explicit Intent)
        btnBack.setOnClickListener(v -> finish());
    }

    /** Reads SharedPreferences and sets toggle/radio states accordingly. */
    private void loadSettings() {
        // ToggleButton checked state (Lecture 7)
        tbMusic.setChecked(sharedPreferences.getBoolean(Constants.KEY_MUSIC_ENABLED, true));
        tbSFX.setChecked(sharedPreferences.getBoolean(Constants.KEY_SFX_ENABLED,   true));

        // RadioButton selection (Lecture 7)
        String diff = sharedPreferences.getString(Constants.KEY_DIFFICULTY, Constants.DIFF_EASY);
        switch (diff) {
            case Constants.DIFF_MEDIUM: rbMedium.setChecked(true); break;
            case Constants.DIFF_HARD:   rbHard.setChecked(true);   break;
            default:                    rbEasy.setChecked(true);   break;
        }
    }

    /** Shows a confirmation dialog before wiping all scores. */
    private void showResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_reset_title)
                .setMessage(R.string.dialog_reset_msg)
                .setPositiveButton(R.string.btn_reset, (dialog, which) -> {
                    clearAllScores();
                    Toast.makeText(this, R.string.scores_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    /** Deletes all high-score and leaderboard entries from SharedPreferences. */
    private void clearAllScores() {
        editor
            .remove(Constants.KEY_SCORE_MULT_EASY).remove(Constants.KEY_SCORE_MULT_MEDIUM)
            .remove(Constants.KEY_SCORE_MULT_HARD)
            .remove(Constants.KEY_SCORE_MEM_EASY).remove(Constants.KEY_SCORE_MEM_MEDIUM)
            .remove(Constants.KEY_SCORE_MEM_HARD)
            .remove(Constants.KEY_SCORE_SCR_EASY).remove(Constants.KEY_SCORE_SCR_MEDIUM)
            .remove(Constants.KEY_SCORE_SCR_HARD);

        // Clear leaderboard top-5 for every game
        String[] prefixes = { Constants.LB_MULT_PREFIX, Constants.LB_MEM_PREFIX, Constants.LB_SCR_PREFIX };
        for (String prefix : prefixes) {
            for (int i = 0; i < 5; i++) {
                editor.remove(prefix + "score_" + i);
                editor.remove(prefix + "name_"  + i);
                editor.remove(prefix + "diff_"  + i);
            }
        }
        editor.apply();
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    @Override protected void onPause()   { super.onPause();   }
    @Override protected void onResume()  { super.onResume();  }
    @Override protected void onDestroy() { super.onDestroy(); }
}
