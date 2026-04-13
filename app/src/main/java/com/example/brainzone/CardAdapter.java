package com.example.brainzone;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecyclerView Adapter for the Memory Match card grid (Game 2).
 *
 * Each item shows either the card's back face (hidden, coloured background)
 * or the card's front face (number shown, white background).
 *
 * Concepts from Lecture 11 – RecyclerView:
 *   • Extends RecyclerView.Adapter<ViewHolder>
 *   • Implements onCreateViewHolder(), onBindViewHolder(), getItemCount()
 *   • ViewHolder stores references to avoid repeated findViewById() calls
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    // ── Interface so Game2Activity can react to card taps ────────────────────
    public interface OnCardClickListener {
        void onCardClick(int position);
    }

    private final Context            context;
    private final List<Card>         cards;
    private final OnCardClickListener listener;

    public CardAdapter(Context context, List<Card> cards, OnCardClickListener listener) {
        this.context  = context;
        this.cards    = cards;
        this.listener = listener;
    }

    // ── Step 1: Create the ViewHolder (inflates item_card.xml) ───────────────
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    // ── Step 2: Bind data to the ViewHolder for this position ────────────────
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);

        if (card.isMatched()) {
            // Matched cards: show value, green background, not clickable
            holder.tvCardValue.setText(String.valueOf(card.getValue()));
            holder.tvCardValue.setVisibility(View.VISIBLE);
            holder.cardView.setCardBackgroundColor(Color.parseColor("#4CAF50"));
            holder.cardView.setEnabled(false);
            holder.tvCardValue.setTextColor(Color.WHITE);

        } else if (card.isFaceUp()) {
            // Flipped but not yet matched: show value, white background
            holder.tvCardValue.setText(String.valueOf(card.getValue()));
            holder.tvCardValue.setVisibility(View.VISIBLE);
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.cardView.setEnabled(false); // disable during check
            holder.tvCardValue.setTextColor(Color.parseColor("#6C63FF"));

        } else {
            // Face-down: hide value, purple background
            holder.tvCardValue.setVisibility(View.INVISIBLE);
            holder.cardView.setCardBackgroundColor(Color.parseColor("#6C63FF"));
            holder.cardView.setEnabled(true);
            holder.cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(holder.getAdapterPosition());
                }
            });
        }
    }

    // ── Step 3: Total number of items ────────────────────────────────────────
    @Override
    public int getItemCount() {
        return cards.size();
    }

    // ── ViewHolder: caches view references for one card item ─────────────────
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCardValue;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView    = itemView.findViewById(R.id.cardView);
            tvCardValue = itemView.findViewById(R.id.tvCardValue);
        }
    }
}
