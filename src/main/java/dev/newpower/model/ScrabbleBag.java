package dev.newpower.model;

import java.util.*;

/**
 * Represents the bag of Scrabble tiles with all 100 tiles, their point values, and counts.
 * Provides functionality for random tile selection and hand management.
 */
public class ScrabbleBag {
    private final List<ScrabbleTile> tiles;
    private final Random random;
    
    // Standard Scrabble tile distribution and point values
    private static final Map<Character, Integer> LETTER_POINTS = Map.ofEntries(
        Map.entry('A', 1), Map.entry('B', 3), Map.entry('C', 3), Map.entry('D', 2),
        Map.entry('E', 1), Map.entry('F', 4), Map.entry('G', 2), Map.entry('H', 4),
        Map.entry('I', 1), Map.entry('J', 8), Map.entry('K', 5), Map.entry('L', 1),
        Map.entry('M', 3), Map.entry('N', 1), Map.entry('O', 1), Map.entry('P', 3),
        Map.entry('Q', 10), Map.entry('R', 1), Map.entry('S', 1), Map.entry('T', 1),
        Map.entry('U', 1), Map.entry('V', 4), Map.entry('W', 4), Map.entry('X', 8),
        Map.entry('Y', 4), Map.entry('Z', 10), Map.entry(' ', 0)  // Blank tiles worth 0 points
    );
    
    // Standard Scrabble tile counts (100 tiles total including 2 blank tiles)
    private static final Map<Character, Integer> LETTER_COUNTS = Map.ofEntries(
        Map.entry('A', 9), Map.entry('B', 2), Map.entry('C', 2), Map.entry('D', 4),
        Map.entry('E', 12), Map.entry('F', 2), Map.entry('G', 3), Map.entry('H', 2),
        Map.entry('I', 9), Map.entry('J', 1), Map.entry('K', 1), Map.entry('L', 4),
        Map.entry('M', 2), Map.entry('N', 6), Map.entry('O', 8), Map.entry('P', 2),
        Map.entry('Q', 1), Map.entry('R', 6), Map.entry('S', 4), Map.entry('T', 6),
        Map.entry('U', 4), Map.entry('V', 2), Map.entry('W', 2), Map.entry('X', 1),
        Map.entry('Y', 2), Map.entry('Z', 1), Map.entry(' ', 2)  // 2 blank tiles
    );

    public ScrabbleBag() {
        this.tiles = new ArrayList<>();
        this.random = new Random();
        initializeBag();
    }

    /**
     * Initializes the bag with all 100 Scrabble tiles according to standard distribution.
     */
    private void initializeBag() {
        for (Map.Entry<Character, Integer> entry : LETTER_COUNTS.entrySet()) {
            char letter = entry.getKey();
            int count = entry.getValue();
            int points = LETTER_POINTS.get(letter);
            
            for (int i = 0; i < count; i++) {
                tiles.add(new ScrabbleTile(letter, points, count));
            }
        }
        
        // Verify we have exactly 100 tiles
        if (tiles.size() != 100) {
            throw new IllegalStateException("Bag should contain exactly 100 tiles, but has: " + tiles.size());
        }
    }

    /**
     * Draws a random tile from the bag.
     * @return A random ScrabbleTile, or null if the bag is empty
     */
    public ScrabbleTile drawTile() {
        if (tiles.isEmpty()) {
            return null;
        }
        
        int index = random.nextInt(tiles.size());
        return tiles.remove(index);
    }

    /**
     * Draws multiple tiles for a player's hand.
     * @param count The number of tiles to draw
     * @return A list of drawn tiles (may be smaller than requested if bag is nearly empty)
     */
    public List<ScrabbleTile> drawTiles(int count) {
        List<ScrabbleTile> drawnTiles = new ArrayList<>();
        int tilesToDraw = Math.min(count, tiles.size());
        
        for (int i = 0; i < tilesToDraw; i++) {
            ScrabbleTile tile = drawTile();
            if (tile != null) {
                drawnTiles.add(tile);
            }
        }
        
        return drawnTiles;
    }

    /**
     * Returns tiles to the bag (for example, when a player exchanges tiles).
     * @param tilesToReturn The tiles to return to the bag
     */
    public void returnTiles(List<ScrabbleTile> tilesToReturn) {
        if (tilesToReturn != null) {
            tiles.addAll(tilesToReturn);
        }
    }

    /**
     * Returns a single tile to the bag.
     * @param tile The tile to return
     */
    public void returnTile(ScrabbleTile tile) {
        if (tile != null) {
            tiles.add(tile);
        }
    }

    /**
     * Removes a tile with the specified letter from the bag.
     * @param letter The letter of the tile to remove
     * @return The removed tile, or null if not found
     */
    public ScrabbleTile removeTileWithLetter(char letter) {
        for (int i = 0; i < tiles.size(); i++) {
            ScrabbleTile tile = tiles.get(i);
            if (tile.getLetter() == letter) {
                return tiles.remove(i);
            }
        }
        return null;
    }

    /**
     * Gets the number of tiles remaining in the bag.
     * @return The number of tiles left
     */
    public int getRemainingTiles() {
        return tiles.size();
    }

    /**
     * Checks if the bag is empty.
     * @return true if no tiles remain, false otherwise
     */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    /**
     * Gets the point value for a given letter.
     * @param letter The letter to get points for
     * @return The point value, or 0 if the letter is not valid
     */
    public static int getPointValue(char letter) {
        return LETTER_POINTS.getOrDefault(Character.toUpperCase(letter), 0);
    }

    /**
     * Gets the count of a given letter in the original bag.
     * @param letter The letter to get count for
     * @return The count of that letter, or 0 if the letter is not valid
     */
    public static int getLetterCount(char letter) {
        return LETTER_COUNTS.getOrDefault(Character.toUpperCase(letter), 0);
    }

    /**
     * Gets a copy of all remaining tiles (for debugging/information purposes).
     * @return A new list containing all remaining tiles
     */
    public List<ScrabbleTile> getRemainingTilesList() {
        return new ArrayList<>(tiles);
    }

    /**
     * Shuffles the remaining tiles in the bag.
     */
    public void shuffle() {
        Collections.shuffle(tiles, random);
    }

    /**
     * Resets the bag to its initial state with all 100 tiles.
     */
    public void reset() {
        tiles.clear();
        initializeBag();
    }

    @Override
    public String toString() {
        return "ScrabbleBag{remainingTiles=" + tiles.size() + "}";
    }
} 