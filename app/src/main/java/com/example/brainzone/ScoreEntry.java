package com.example.brainzone;

/**
 * Model class for a leaderboard entry.
 * Used by LeaderboardAdapter to display ranked scores.
 */
public class ScoreEntry {

    private int    rank;        // 1-5
    private String playerName;
    private int    score;
    private String difficulty;

    public ScoreEntry(int rank, String playerName, int score, String difficulty) {
        this.rank       = rank;
        this.playerName = playerName;
        this.score      = score;
        this.difficulty = difficulty;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int    getRank()        { return rank;       }
    public String getPlayerName()  { return playerName; }
    public int    getScore()       { return score;      }
    public String getDifficulty()  { return difficulty; }
}
