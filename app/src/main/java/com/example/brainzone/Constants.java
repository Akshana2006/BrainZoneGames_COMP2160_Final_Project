package com.example.brainzone;

/**
 * Constants used across the entire app.
 * Centralising keys avoids typos and keeps SharedPreferences consistent.
 */
public class Constants {

    // ── SharedPreferences file name ──────────────────────────────────────────
    public static final String PREFS_NAME = "BrainZonePrefs";

    // ── Player ───────────────────────────────────────────────────────────────
    public static final String KEY_PLAYER_NAME    = "player_name";

    // ── Settings ─────────────────────────────────────────────────────────────
    public static final String KEY_MUSIC_ENABLED  = "music_enabled";
    public static final String KEY_SFX_ENABLED    = "sfx_enabled";
    public static final String KEY_DIFFICULTY     = "difficulty";

    public static final String DIFF_EASY   = "easy";
    public static final String DIFF_MEDIUM = "medium";
    public static final String DIFF_HARD   = "hard";

    // ── High-Score keys (per game, per difficulty) ────────────────────────────
    public static final String KEY_SCORE_MULT_EASY    = "score_multiplication_easy";
    public static final String KEY_SCORE_MULT_MEDIUM  = "score_multiplication_medium";
    public static final String KEY_SCORE_MULT_HARD    = "score_multiplication_hard";

    public static final String KEY_SCORE_MEM_EASY     = "score_memory_easy";
    public static final String KEY_SCORE_MEM_MEDIUM   = "score_memory_medium";
    public static final String KEY_SCORE_MEM_HARD     = "score_memory_hard";

    public static final String KEY_SCORE_SCR_EASY     = "score_scramble_easy";
    public static final String KEY_SCORE_SCR_MEDIUM   = "score_scramble_medium";
    public static final String KEY_SCORE_SCR_HARD     = "score_scramble_hard";

    // ── Leaderboard keys (top-5 stored as score_0 … score_4) ────────────────
    public static final String LB_MULT_PREFIX = "lb_mult_";
    public static final String LB_MEM_PREFIX  = "lb_mem_";
    public static final String LB_SCR_PREFIX  = "lb_scr_";

    // ── Intent extras ────────────────────────────────────────────────────────
    public static final String EXTRA_GAME_NAME  = "extra_game_name";
    public static final String EXTRA_SCORE      = "extra_score";
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";
    public static final String EXTRA_TIME       = "extra_time";
    public static final String EXTRA_MOVES      = "extra_moves";
    public static final String EXTRA_ACCURACY   = "extra_accuracy";

    // ── Game names (for display & leaderboard routing) ───────────────────────
    public static final String GAME_MULTIPLICATION = "Multiplication Puzzle";
    public static final String GAME_MEMORY         = "Memory Match";
    public static final String GAME_SCRAMBLE       = "Word Scramble";

    // ── Custom Broadcast action ───────────────────────────────────────────────
    public static final String ACTION_GAME_OVER = "com.example.brainzone.GAME_OVER";

    // ── Notification ─────────────────────────────────────────────────────────
    public static final String MUSIC_CHANNEL_ID = "BrainZoneMusicChannel";
    public static final int    MUSIC_NOTIF_ID   = 101;

    private Constants() { /* utility class – no instances */ }
}
