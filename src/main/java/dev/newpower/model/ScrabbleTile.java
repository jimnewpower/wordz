package dev.newpower.model;

/**
 * Represents a single Scrabble tile with its letter, point value, and count in the game.
 */
public class ScrabbleTile {
    private final char letter;
    private final int pointValue;
    private final int count;

    public ScrabbleTile(char letter, int pointValue, int count) {
        this.letter = letter;
        this.pointValue = pointValue;
        this.count = count;
    }

    public char getLetter() {
        return letter;
    }

    public int getPointValue() {
        return pointValue;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return letter == ' ' ? "BLANK" : String.valueOf(letter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ScrabbleTile that = (ScrabbleTile) obj;
        return letter == that.letter;
    }

    @Override
    public int hashCode() {
        return Character.hashCode(letter);
    }
} 