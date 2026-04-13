package com.example.brainzone;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * LeaderboardActivity – displays Top-5 scores for each of the three games.
 *
 * Concepts:
 *   • SharedPreferences – read stored scores (Lecture 17)
 *   • RecyclerView with LinearLayoutManager (Lecture 11)
 *   • Explicit Intent navigation (Lecture 8)
 */
public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvMult, rvMem, rvScr;
    private Button       btnClearAll, btnBack;
    private TextView     tvNoMult, tvNoMem, tvNoScr;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // ── Bind views ────────────────────────────────────────────────────────
        rvMult    = findViewById(R.id.rvMultiplication);
        rvMem     = findViewById(R.id.rvMemory);
        rvScr     = findViewById(R.id.rvScramble);
        tvNoMult  = findViewById(R.id.tvNoMult);
        tvNoMem   = findViewById(R.id.tvNoMem);
        tvNoScr   = findViewById(R.id.tvNoScr);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnBack     = findViewById(R.id.btnBack);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // LinearLayoutManager for each RecyclerView (Lecture 11)
        rvMult.setLayoutManager(new LinearLayoutManager(this));
        rvMem.setLayoutManager(new LinearLayoutManager(this));
        rvScr.setLayoutManager(new LinearLayoutManager(this));

        loadLeaderboards();

        btnClearAll.setOnClickListener(v -> showClearDialog());
        btnBack.setOnClickListener(v -> finish());
    }

    /** Loads top-5 scores for all three games from SharedPreferences. */
    private void loadLeaderboards() {
        setupRecycler(rvMult, tvNoMult, Constants.LB_MULT_PREFIX);
        setupRecycler(rvMem,  tvNoMem,  Constants.LB_MEM_PREFIX);
        setupRecycler(rvScr,  tvNoScr,  Constants.LB_SCR_PREFIX);
    }

    /**
     * Reads up to 5 entries from SharedPreferences for a given game prefix
     * and sets the RecyclerView adapter.
     */
    private void setupRecycler(RecyclerView rv, TextView tvEmpty, String prefix) {
        List<ScoreEntry> entries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int    score = sharedPreferences.getInt(prefix + "score_" + i, -1);
            String name  = sharedPreferences.getString(prefix + "name_"  + i, "");
            String diff  = sharedPreferences.getString(prefix + "diff_"  + i, "");
            if (score >= 0 && !name.isEmpty()) {
                entries.add(new ScoreEntry(i + 1, name, score, diff));
            }
        }

        if (entries.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            rv.setVisibility(android.view.View.GONE);
        } else {
            tvEmpty.setVisibility(android.view.View.GONE);
            rv.setVisibility(android.view.View.VISIBLE);
            rv.setAdapter(new LeaderboardAdapter(this, entries));
        }
    }

    /** Confirmation dialog before clearing all scores. */
    private void showClearDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_reset_title)
                .setMessage(R.string.dialog_clear_lb_msg)
                .setPositiveButton(R.string.btn_clear, (d, w) -> {
                    clearAllLeaderboards();
                    loadLeaderboards(); // Refresh UI
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    /** Wipes all leaderboard entries from SharedPreferences (Lecture 17 – delete). */
    private void clearAllLeaderboards() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
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

    /**
     * Saves a new score to the leaderboard (top-5 sorted, descending).
     * Called from GameResultActivity via static helper.
     */
    public static void saveScore(SharedPreferences prefs, String prefix,
                                  String playerName, int newScore, String difficulty) {
        SharedPreferences.Editor editor = prefs.edit();

        // Load existing top-5 into parallel arrays
        int[]    scores = new int[5];
        String[] names  = new String[5];
        String[] diffs  = new String[5];

        for (int i = 0; i < 5; i++) {
            scores[i] = prefs.getInt(prefix + "score_" + i, -1);
            names[i]  = prefs.getString(prefix + "name_"  + i, "");
            diffs[i]  = prefs.getString(prefix + "diff_"  + i, "");
        }

        // Find the insertion position (descending order)
        int insertAt = -1;
        for (int i = 0; i < 5; i++) {
            if (newScore > scores[i]) { insertAt = i; break; }
        }

        if (insertAt >= 0) {
            // Shift lower entries down
            for (int i = 4; i > insertAt; i--) {
                scores[i] = scores[i - 1];
                names[i]  = names[i - 1];
                diffs[i]  = diffs[i - 1];
            }
            scores[insertAt] = newScore;
            names[insertAt]  = playerName;
            diffs[insertAt]  = difficulty;

            // Write back
            for (int i = 0; i < 5; i++) {
                editor.putInt(prefix + "score_" + i, scores[i]);
                editor.putString(prefix + "name_"  + i, names[i]);
                editor.putString(prefix + "diff_"  + i, diffs[i] != null ? diffs[i] : "");
            }
            editor.apply();
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    @Override protected void onPause()   { super.onPause();   }
    @Override protected void onResume()  { super.onResume();  }
    @Override protected void onDestroy() { super.onDestroy(); }
}
