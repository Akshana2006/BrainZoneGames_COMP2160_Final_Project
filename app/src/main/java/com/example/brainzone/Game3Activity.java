package com.example.brainzone;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Game3Activity – Word Scramble.
 *
 * Concepts:
 *   • CountDownTimer per word                 (Lecture 4 – countdown reference)
 *   • SharedPreferences – save high score     (Lecture 17)
 *   • BroadcastReceiver – ScreenOffReceiver   (Lecture 18)
 *   • Explicit Intent to GameResultActivity   (Lecture 8)
 *   • String arrays for word data             (Lecture 5 – strings.xml)
 *
 * Scoring: +15 (1st attempt) | +8 (2nd) | +3 (3rd) | 0 (fail)
 * Hint: reveals one correct letter, costs 5 points (once per word)
 */
public class Game3Activity extends AppCompatActivity implements ScreenOffReceiver.GamePauseListener {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView    tvWordNum, tvCategory, tvScrambled, tvScore,
                        tvAttempts, tvTimer, tvHintLetter;
    private EditText    etAnswer;
    private Button      btnSubmit, btnHint;
    private ProgressBar pbTimer;

    // ── Word Data ─────────────────────────────────────────────────────────────
    private static final String[][] WORD_DATA = {
        // {word, category}
        // Animals (10+)
        {"ELEPHANT",  "Animal"}, {"GIRAFFE",  "Animal"}, {"PENGUIN",  "Animal"},
        {"DOLPHIN",   "Animal"}, {"LEOPARD",  "Animal"}, {"HAMSTER",  "Animal"},
        {"GORILLA",   "Animal"}, {"FLAMINGO", "Animal"}, {"CHEETAH",  "Animal"},
        {"PANTHER",   "Animal"}, {"CROCODILE","Animal"}, {"KANGAROO", "Animal"},
        // Countries (10+)
        {"BRAZIL",    "Country"}, {"FRANCE",  "Country"}, {"CANADA",  "Country"},
        {"GERMANY",   "Country"}, {"JAPAN",   "Country"}, {"MEXICO",  "Country"},
        {"SWEDEN",    "Country"}, {"NIGERIA", "Country"}, {"TURKEY",  "Country"},
        {"PORTUGAL",  "Country"}, {"EGYPT",   "Country"}, {"DENMARK", "Country"},
        // Food & Drinks (10+)
        {"PIZZA",     "Food & Drink"}, {"BURGER",  "Food & Drink"}, {"SUSHI",   "Food & Drink"},
        {"MANGO",     "Food & Drink"}, {"PASTA",   "Food & Drink"}, {"WAFFLE",  "Food & Drink"},
        {"ORANGE",    "Food & Drink"}, {"LEMON",   "Food & Drink"}, {"BANANA",  "Food & Drink"},
        {"COFFEE",    "Food & Drink"}, {"BISCUIT", "Food & Drink"}, {"NOODLES", "Food & Drink"},
        // Technology (10+)
        {"LAPTOP",    "Technology"}, {"TABLET",   "Technology"}, {"ROUTER", "Technology"},
        {"BROWSER",   "Technology"}, {"SERVER",   "Technology"}, {"PYTHON", "Technology"},
        {"ANDROID",   "Technology"}, {"NETWORK",  "Technology"}, {"CURSOR", "Technology"},
        {"DATABASE",  "Technology"}, {"KEYBOARD", "Technology"}, {"MONITOR","Technology"}
    };

    // ── Game state ────────────────────────────────────────────────────────────
    private List<String[]> shuffledWords;
    private int   wordIndex     = 0;
    private int   totalScore    = 0;
    private int   attempts      = 0;   // attempts for current word (1-3)
    private int   timerSeconds;
    private int   correctAnswers= 0;
    private boolean hintUsedThisWord = false;
    private boolean gamePaused       = false;
    private boolean timerRunning     = false;

    private String currentWord;
    private String currentCategory;
    private String scrambledWord;
    private String revealedLetter = "";

    private static final int TOTAL_WORDS    = 10;
    private static final int MAX_ATTEMPTS   = 3;
    private static final int HINT_COST      = 5;

    private CountDownTimer countDownTimer;
    private ScreenOffReceiver screenOffReceiver;
    private SharedPreferences sharedPreferences;
    private String difficulty;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game3);

        // ── Bind views ────────────────────────────────────────────────────────
        tvWordNum     = findViewById(R.id.tvWordNum);
        tvCategory    = findViewById(R.id.tvCategory);
        tvScrambled   = findViewById(R.id.tvScrambled);
        tvScore       = findViewById(R.id.tvScore);
        tvAttempts    = findViewById(R.id.tvAttempts);
        tvTimer       = findViewById(R.id.tvTimer);
        tvHintLetter  = findViewById(R.id.tvHintLetter);
        etAnswer      = findViewById(R.id.etAnswer);
        btnSubmit     = findViewById(R.id.btnSubmit);
        btnHint       = findViewById(R.id.btnHint);
        pbTimer       = findViewById(R.id.pbTimer);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        difficulty = getIntent().getStringExtra(Constants.EXTRA_DIFFICULTY);
        if (difficulty == null) difficulty = Constants.DIFF_EASY;

        switch (difficulty) {
            case Constants.DIFF_HARD:   timerSeconds = 12; break;
            case Constants.DIFF_MEDIUM: timerSeconds = 20; break;
            default:                    timerSeconds = 30; break;
        }

        // Shuffle the full word list then take the first TOTAL_WORDS
        shuffledWords = new ArrayList<>(Arrays.asList(WORD_DATA));
        Collections.shuffle(shuffledWords);

        screenOffReceiver = new ScreenOffReceiver(this);

        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnHint.setOnClickListener(v   -> useHint());

        loadNextWord();
    }

    // ── Word logic ────────────────────────────────────────────────────────────

    private void loadNextWord() {
        if (wordIndex >= TOTAL_WORDS) {
            endGame();
            return;
        }

        String[] wordData = shuffledWords.get(wordIndex);
        currentWord     = wordData[0].toUpperCase();
        currentCategory = wordData[1];
        wordIndex++;

        attempts         = 0;
        hintUsedThisWord = false;
        revealedLetter   = "";
        tvHintLetter.setText("");
        etAnswer.setText("");
        btnHint.setEnabled(true);

        scrambledWord = scramble(currentWord);

        tvWordNum.setText(getString(R.string.word_num, wordIndex, TOTAL_WORDS));
        tvCategory.setText(getString(R.string.category_label, currentCategory));
        tvScrambled.setText(scrambledWord);
        tvScore.setText(getString(R.string.score_label, totalScore));
        tvAttempts.setText(getString(R.string.attempts_left, MAX_ATTEMPTS - attempts));

        startTimer();
    }

    /**
     * Scrambles a word by shuffling its character array.
     * Re-shuffles if the result equals the original (Lecture requirement).
     */
    private String scramble(String word) {
        List<Character> chars = new ArrayList<>();
        for (char c : word.toCharArray()) chars.add(c);

        String scrambled;
        int tries = 0;
        do {
            Collections.shuffle(chars);
            StringBuilder sb = new StringBuilder();
            for (char c : chars) sb.append(c);
            scrambled = sb.toString();
            tries++;
        } while (scrambled.equals(word) && tries < 20);

        return scrambled;
    }

    private void startTimer() {
        timerRunning = true;
        pbTimer.setMax(timerSeconds);
        pbTimer.setProgress(timerSeconds);

        countDownTimer = new CountDownTimer(timerSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int sLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText(getString(R.string.timer_label, sLeft));
                pbTimer.setProgress(sLeft);
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                Toast.makeText(Game3Activity.this,
                        getString(R.string.time_up_word, currentWord), Toast.LENGTH_SHORT).show();
                loadNextWord();
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
        timerRunning = false;
    }

    /** Evaluates the player's submitted answer. */
    private void checkAnswer() {
        if (!timerRunning) return;
        String input = etAnswer.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, R.string.enter_answer, Toast.LENGTH_SHORT).show();
            return;
        }

        attempts++;
        tvAttempts.setText(getString(R.string.attempts_left, MAX_ATTEMPTS - attempts));

        // Case-insensitive comparison
        if (input.equalsIgnoreCase(currentWord)) {
            cancelTimer();
            correctAnswers++;
            int points;
            if (attempts == 1)      points = 15;
            else if (attempts == 2) points = 8;
            else                    points = 3;

            if (hintUsedThisWord) points = Math.max(0, points - HINT_COST);
            totalScore += points;

            Toast.makeText(this, getString(R.string.correct, totalScore), Toast.LENGTH_SHORT).show();
            loadNextWord();

        } else if (attempts >= MAX_ATTEMPTS) {
            // 3 failures – show answer and move on
            cancelTimer();
            Toast.makeText(this,
                    getString(R.string.wrong_word, currentWord), Toast.LENGTH_SHORT).show();
            loadNextWord();

        } else {
            etAnswer.setText("");
            Toast.makeText(this,
                    getString(R.string.try_again, MAX_ATTEMPTS - attempts), Toast.LENGTH_SHORT).show();
        }
    }

    /** Reveals one letter of the correct word, costs HINT_COST points. */
    private void useHint() {
        if (hintUsedThisWord) return;
        hintUsedThisWord = true;
        btnHint.setEnabled(false);

        // Pick a random position to reveal
        int pos = random.nextInt(currentWord.length());
        revealedLetter = "Hint: Letter " + (pos + 1) + " is '" + currentWord.charAt(pos) + "'";
        tvHintLetter.setText(revealedLetter);

        totalScore = Math.max(0, totalScore - HINT_COST);
        tvScore.setText(getString(R.string.score_label, totalScore));
        Toast.makeText(this, getString(R.string.hint_used, HINT_COST), Toast.LENGTH_SHORT).show();
    }

    private void endGame() {
        cancelTimer();
        int accuracy = (correctAnswers * 100) / TOTAL_WORDS;
        saveHighScore();
        sendGameOverBroadcast();

        Intent intent = new Intent(this, GameResultActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_NAME,  Constants.GAME_SCRAMBLE);
        intent.putExtra(Constants.EXTRA_SCORE,      totalScore);
        intent.putExtra(Constants.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(Constants.EXTRA_ACCURACY,   accuracy);
        startActivity(intent);
        finish();
    }

    // ── SharedPreferences (Lecture 17) ────────────────────────────────────────
    private void saveHighScore() {
        String key;
        switch (difficulty) {
            case Constants.DIFF_MEDIUM: key = Constants.KEY_SCORE_SCR_MEDIUM; break;
            case Constants.DIFF_HARD:   key = Constants.KEY_SCORE_SCR_HARD;   break;
            default:                    key = Constants.KEY_SCORE_SCR_EASY;   break;
        }
        int previous = sharedPreferences.getInt(key, 0);
        if (totalScore > previous) {
            sharedPreferences.edit().putInt(key, totalScore).apply();
        }
        String playerName = sharedPreferences.getString(Constants.KEY_PLAYER_NAME, "Player");
        LeaderboardActivity.saveScore(sharedPreferences,
                Constants.LB_SCR_PREFIX, playerName, totalScore, difficulty);
    }

    private void sendGameOverBroadcast() {
        Intent broadcastIntent = new Intent(Constants.ACTION_GAME_OVER);
        broadcastIntent.putExtra(Constants.EXTRA_GAME_NAME, Constants.GAME_SCRAMBLE);
        broadcastIntent.putExtra(Constants.EXTRA_SCORE, totalScore);
        sendBroadcast(broadcastIntent);
    }

    // ── ScreenOffReceiver ─────────────────────────────────────────────────────
    @Override
    public void onScreenOff() {
        gamePaused = true;
        cancelTimer();
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
        cancelTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        try { unregisterReceiver(screenOffReceiver); } catch (Exception ignored) {}
    }

    @Override
    public void onBackPressed() {
        cancelTimer();
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_quit_title)
                .setMessage(R.string.dialog_quit_msg)
                .setPositiveButton(R.string.btn_yes, (d, w) -> finish())
                .setNegativeButton(R.string.btn_no, (d, w) -> {
                    if (!gamePaused) startTimer();
                })
                .show();
    }
}
