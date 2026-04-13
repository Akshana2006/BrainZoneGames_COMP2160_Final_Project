package com.example.brainzone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Game2Activity – Memory Match (Flip Cards).
 *
 * Concepts demonstrated:
 *   • RecyclerView with GridLayoutManager (Lecture 11)
 *   • Custom Adapter (CardAdapter) with ViewHolder   (Lecture 11)
 *   • Runnable + Handler for the elapsed-time timer  (Lecture 14)
 *   • BroadcastReceiver – ScreenOffReceiver          (Lecture 18)
 *   • SharedPreferences – save best move count       (Lecture 17)
 *   • Explicit Intent to GameResultActivity          (Lecture 8)
 *
 * Grid sizes: Easy=4×3, Medium=4×4, Hard=4×5
 * Score formula: 1000 - (extraMoves * 10) - (elapsedSeconds * 2)
 */
public class Game2Activity extends AppCompatActivity implements ScreenOffReceiver.GamePauseListener {

    // ── Views ─────────────────────────────────────────────────────────────────
    private RecyclerView rvCards;
    private TextView     tvMoves, tvTime, tvPairs;
    private Button       btnBack;

    // ── Game state ────────────────────────────────────────────────────────────
    private List<Card>  cards;
    private CardAdapter cardAdapter;
    private int         firstFlippedIndex  = -1;
    private int         secondFlippedIndex = -1;
    private boolean     isChecking         = false; // Prevent clicks during match check
    private int         moveCount          = 0;
    private int         matchedPairs       = 0;
    private int         totalPairs;
    private int         gridColumns;
    private boolean     gamePaused         = false;

    // ── Timer (Runnable – Lecture 14) ─────────────────────────────────────────
    private int     elapsedSeconds = 0;
    private Handler timerHandler   = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    // ── Difficulty ────────────────────────────────────────────────────────────
    private String difficulty;

    private ScreenOffReceiver screenOffReceiver;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);

        // ── Bind views ────────────────────────────────────────────────────────
        rvCards  = findViewById(R.id.rvCards);
        tvMoves  = findViewById(R.id.tvMoves);
        tvTime   = findViewById(R.id.tvTime);
        tvPairs  = findViewById(R.id.tvPairs);
        btnBack  = findViewById(R.id.btnBack);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        // ── Read intent extras (Lecture 8) ────────────────────────────────────
        difficulty = getIntent().getStringExtra(Constants.EXTRA_DIFFICULTY);
        if (difficulty == null) difficulty = Constants.DIFF_EASY;

        // ── Configure grid by difficulty ──────────────────────────────────────
        switch (difficulty) {
            case Constants.DIFF_HARD:
                totalPairs  = 10; // 4×5 = 20 cards
                gridColumns = 4;
                break;
            case Constants.DIFF_MEDIUM:
                totalPairs  = 8;  // 4×4 = 16 cards
                gridColumns = 4;
                break;
            default: // Easy
                totalPairs  = 6;  // 4×3 = 12 cards
                gridColumns = 3;
                break;
        }

        // ── Setup ─────────────────────────────────────────────────────────────
        screenOffReceiver = new ScreenOffReceiver(this);

        buildCardList();
        setupRecyclerView();
        startTimer();
        updateUI();

        btnBack.setOnClickListener(v -> confirmQuit());
    }

    // ── Card setup ────────────────────────────────────────────────────────────

    /** Creates a shuffled list of paired cards. */
    private void buildCardList() {
        cards = new ArrayList<>();
        for (int i = 1; i <= totalPairs; i++) {
            cards.add(new Card(i)); // Each value appears twice (pair)
            cards.add(new Card(i));
        }
        Collections.shuffle(cards); // Randomise positions
    }

    /** Connects the RecyclerView with a GridLayoutManager and CardAdapter (Lecture 11). */
    private void setupRecyclerView() {
        rvCards.setLayoutManager(new GridLayoutManager(this, gridColumns));
        cardAdapter = new CardAdapter(this, cards, this::onCardClicked);
        rvCards.setAdapter(cardAdapter);
    }

    // ── Card click handler ────────────────────────────────────────────────────

    /**
     * Called by CardAdapter when a face-down card is tapped.
     * Implements the core match / no-match logic.
     */
    private void onCardClicked(int position) {
        if (isChecking || gamePaused) return;
        if (cards.get(position).isFaceUp() || cards.get(position).isMatched()) return;

        // Flip the tapped card face-up
        cards.get(position).setFaceUp(true);
        cardAdapter.notifyItemChanged(position);

        if (firstFlippedIndex == -1) {
            // This is the first card of the pair
            firstFlippedIndex = position;
        } else {
            // This is the second card – check for a match
            secondFlippedIndex = position;
            moveCount++;
            updateUI();
            checkForMatch();
        }
    }

    /** Checks whether the two flipped cards match. */
    private void checkForMatch() {
        isChecking = true;
        Card first  = cards.get(firstFlippedIndex);
        Card second = cards.get(secondFlippedIndex);

        if (first.getValue() == second.getValue()) {
            // ── Match found ───────────────────────────────────────────────────
            first.setMatched(true);
            second.setMatched(true);
            matchedPairs++;
            cardAdapter.notifyItemChanged(firstFlippedIndex);
            cardAdapter.notifyItemChanged(secondFlippedIndex);
            firstFlippedIndex  = -1;
            secondFlippedIndex = -1;
            isChecking         = false;

            if (matchedPairs == totalPairs) {
                endGame(); // All pairs found!
            } else {
                Toast.makeText(this, R.string.match_found, Toast.LENGTH_SHORT).show();
            }
        } else {
            // ── No match – flip back after 900 ms ────────────────────────────
            final int fIdx = firstFlippedIndex;
            final int sIdx = secondFlippedIndex;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                cards.get(fIdx).setFaceUp(false);
                cards.get(sIdx).setFaceUp(false);
                cardAdapter.notifyItemChanged(fIdx);
                cardAdapter.notifyItemChanged(sIdx);
                firstFlippedIndex  = -1;
                secondFlippedIndex = -1;
                isChecking         = false;
            }, 900);
        }
    }

    // ── Timer (Runnable + Handler from Lecture 14) ────────────────────────────

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!gamePaused) {
                    elapsedSeconds++;
                    updateUI();
                }
                timerHandler.postDelayed(this, 1000); // run again in 1 s
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void updateUI() {
        tvMoves.setText(getString(R.string.moves_label, moveCount));
        tvTime.setText(getString(R.string.time_elapsed, elapsedSeconds));
        tvPairs.setText(getString(R.string.pairs_label, matchedPairs, totalPairs));
    }

    // ── Game end ──────────────────────────────────────────────────────────────

    private void endGame() {
        stopTimer();

        // Score = 1000 - penalty for extra moves - penalty for time
        int basePairs  = totalPairs;
        int extraMoves = Math.max(0, moveCount - basePairs);
        int score      = Math.max(0, 1000 - (extraMoves * 10) - (elapsedSeconds * 2));

        saveHighScore(moveCount);
        sendGameOverBroadcast(score);

        Intent intent = new Intent(this, GameResultActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_NAME,  Constants.GAME_MEMORY);
        intent.putExtra(Constants.EXTRA_SCORE,      score);
        intent.putExtra(Constants.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(Constants.EXTRA_MOVES,      moveCount);
        intent.putExtra(Constants.EXTRA_TIME,       elapsedSeconds);
        startActivity(intent);
        finish();
    }

    // ── SharedPreferences – save best move count (Lecture 17) ────────────────
    private void saveHighScore(int moves) {
        String key;
        switch (difficulty) {
            case Constants.DIFF_MEDIUM: key = Constants.KEY_SCORE_MEM_MEDIUM; break;
            case Constants.DIFF_HARD:   key = Constants.KEY_SCORE_MEM_HARD;   break;
            default:                    key = Constants.KEY_SCORE_MEM_EASY;   break;
        }
        // For Memory Match, lower moves = better. Store as score for leaderboard.
        int basePairs  = totalPairs;
        int extraMoves = Math.max(0, moves - basePairs);
        int score      = Math.max(0, 1000 - (extraMoves * 10) - (elapsedSeconds * 2));

        int previous = sharedPreferences.getInt(key, 0);
        if (score > previous) {
            sharedPreferences.edit().putInt(key, score).apply();
        }
        String playerName = sharedPreferences.getString(Constants.KEY_PLAYER_NAME, "Player");
        LeaderboardActivity.saveScore(sharedPreferences,
                Constants.LB_MEM_PREFIX, playerName, score, difficulty);
    }

    private void sendGameOverBroadcast(int score) {
        Intent broadcastIntent = new Intent(Constants.ACTION_GAME_OVER);
        broadcastIntent.putExtra(Constants.EXTRA_GAME_NAME, Constants.GAME_MEMORY);
        broadcastIntent.putExtra(Constants.EXTRA_SCORE, score);
        sendBroadcast(broadcastIntent);
    }

    // ── ScreenOffReceiver ─────────────────────────────────────────────────────
    @Override
    public void onScreenOff() {
        gamePaused = true;
        Toast.makeText(this, R.string.game_paused, Toast.LENGTH_SHORT).show();
    }

    // ── Lifecycle (Lecture 4) ──────────────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenOffReceiver, filter);
        gamePaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        try { unregisterReceiver(screenOffReceiver); } catch (Exception ignored) {}
    }

    private void confirmQuit() {
        stopTimer();
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_quit_title)
                .setMessage(R.string.dialog_quit_msg)
                .setPositiveButton(R.string.btn_yes, (d, w) -> finish())
                .setNegativeButton(R.string.btn_no, (d, w) -> {
                    gamePaused = false;
                    startTimer();
                })
                .show();
    }

    @Override
    public void onBackPressed() { confirmQuit(); }
}
