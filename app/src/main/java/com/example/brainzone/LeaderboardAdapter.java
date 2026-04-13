package com.example.brainzone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecyclerView Adapter for the Leaderboard screen.
 *
 * Displays a ranked list of ScoreEntry objects.
 * Concepts from Lecture 11 – RecyclerView.
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ScoreViewHolder> {

    private final Context         context;
    private final List<ScoreEntry> scores;

    public LeaderboardAdapter(Context context, List<ScoreEntry> scores) {
        this.context = context;
        this.scores  = scores;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        ScoreEntry entry = scores.get(position);
        holder.tvRank.setText(String.valueOf(entry.getRank()));
        holder.tvName.setText(entry.getPlayerName());
        holder.tvScore.setText(String.valueOf(entry.getScore()));
        holder.tvDifficulty.setText(entry.getDifficulty().toUpperCase());
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────
    public static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore, tvDifficulty;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank       = itemView.findViewById(R.id.tvRank);
            tvName       = itemView.findViewById(R.id.tvName);
            tvScore      = itemView.findViewById(R.id.tvScore);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
        }
    }
}
