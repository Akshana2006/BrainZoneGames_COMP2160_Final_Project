package com.example.brainzone;

/**
 * Model class for a Memory Match card.
 * Stores the card's value, its face-up state, and whether it has been matched.
 */
public class Card {

    private int     value;      // The number / symbol on the front face
    private boolean isFaceUp;   // True when the card has been flipped by the player
    private boolean isMatched;  // True when this card has been successfully paired

    public Card(int value) {
        this.value     = value;
        this.isFaceUp  = false;
        this.isMatched = false;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getValue()     { return value;     }
    public boolean isFaceUp() { return isFaceUp;  }
    public boolean isMatched(){ return isMatched; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setFaceUp(boolean faceUp)    { this.isFaceUp  = faceUp;   }
    public void setMatched(boolean matched)  { this.isMatched = matched;  }
}
