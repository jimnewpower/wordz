package dev.newpower.service;

import dev.newpower.model.ScrabbleBag;
import dev.newpower.model.ScrabbleBoard;
import dev.newpower.model.ScrabbleTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for generating Scrabble puzzles with valid words and remaining tiles.
 */
@Service
public class ScrabblePuzzleService {
    
    @Autowired
    private WordDictionaryService wordDictionaryService;
    
    private final ScrabbleBag bag = new ScrabbleBag();
    private final ScrabbleBoard board = new ScrabbleBoard();
    
    /**
     * Generates a new Scrabble puzzle with 93 tiles placed as valid words
     * and returns the remaining 7 tiles as the puzzle.
     */
    public Map<String, Object> generatePuzzle() {
        // Reset everything
        bag.reset();
        board.clear();
        
        // Place words on the board
        placeWordsOnBoard();
        
        // Get remaining tiles (should be 7)
        List<ScrabbleTile> remainingTiles = bag.getRemainingTilesList();
        
        // Create response
        Map<String, Object> puzzle = new HashMap<>();
        puzzle.put("board", getBoardState());
        puzzle.put("remainingTiles", remainingTiles);
        puzzle.put("placedTileCount", board.getPlacedTileCount());
        puzzle.put("remainingTileCount", remainingTiles.size());
        
        return puzzle;
    }
    
    /**
     * Places valid words on the board using tiles from the bag.
     */
    private void placeWordsOnBoard() {
        // Start with a word that goes through the center
        String firstWord = getRandomWord(5);
        placeWordHorizontally(firstWord, 7, 5); // Center row, starting at column 5
        
        // Place additional words
        placeAdditionalWords();
    }
    
    /**
     * Places additional words on the board.
     */
    private void placeAdditionalWords() {
        int attempts = 0;
        int maxAttempts = 100;
        
        while (board.getPlacedTileCount() < 80 && attempts < maxAttempts && bag.getRemainingTiles() > 15) {
            attempts++;
            
            // Try to place a word vertically
            if (Math.random() < 0.5) {
                placeRandomVerticalWord();
            } else {
                placeRandomHorizontalWord();
            }
        }
        
        // If we still have too many tiles, place some simple words
        while (bag.getRemainingTiles() > 7 && attempts < maxAttempts * 2) {
            attempts++;
            placeSimpleWords();
        }
    }
    
    /**
     * Places a random word horizontally on the board.
     */
    private void placeRandomHorizontalWord() {
        String word = getRandomWord(3 + (int)(Math.random() * 5)); // 3-7 letters
        
        // Try to find a valid placement that uses existing tiles
        List<PlacementOption> validPlacements = findValidHorizontalPlacements(word);
        
        if (!validPlacements.isEmpty()) {
            // Choose a random valid placement
            PlacementOption placement = validPlacements.get((int)(Math.random() * validPlacements.size()));
            placeWordHorizontally(word, placement.row, placement.col);
        }
    }
    
    /**
     * Places a random word vertically on the board.
     */
    private void placeRandomVerticalWord() {
        String word = getRandomWord(3 + (int)(Math.random() * 5)); // 3-7 letters
        
        // Try to find a valid placement that uses existing tiles
        List<PlacementOption> validPlacements = findValidVerticalPlacements(word);
        
        if (!validPlacements.isEmpty()) {
            // Choose a random valid placement
            PlacementOption placement = validPlacements.get((int)(Math.random() * validPlacements.size()));
            placeWordVertically(word, placement.row, placement.col);
        }
    }
    
    /**
     * Finds all valid horizontal placements for a word that use existing tiles.
     */
    private List<PlacementOption> findValidHorizontalPlacements(String word) {
        List<PlacementOption> validPlacements = new ArrayList<>();
        
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col <= 15 - word.length(); col++) {
                if (canPlaceWordHorizontally(word, row, col) && 
                    usesExistingTileHorizontally(word, row, col) &&
                    createsValidWordsHorizontally(word, row, col)) {
                    validPlacements.add(new PlacementOption(row, col));
                }
            }
        }
        
        return validPlacements;
    }
    
    /**
     * Finds all valid vertical placements for a word that use existing tiles.
     */
    private List<PlacementOption> findValidVerticalPlacements(String word) {
        List<PlacementOption> validPlacements = new ArrayList<>();
        
        for (int row = 0; row <= 15 - word.length(); row++) {
            for (int col = 0; col < 15; col++) {
                if (canPlaceWordVertically(word, row, col) && 
                    usesExistingTileVertically(word, row, col) &&
                    createsValidWordsVertically(word, row, col)) {
                    validPlacements.add(new PlacementOption(row, col));
                }
            }
        }
        
        return validPlacements;
    }
    
    /**
     * Checks if a horizontal word placement uses at least one existing tile.
     */
    private boolean usesExistingTileHorizontally(String word, int row, int col) {
        for (int i = 0; i < word.length(); i++) {
            if (!board.isEmpty(row, col + i)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a vertical word placement uses at least one existing tile.
     */
    private boolean usesExistingTileVertically(String word, int row, int col) {
        for (int i = 0; i < word.length(); i++) {
            if (!board.isEmpty(row + i, col)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if placing a word horizontally creates valid words in all directions.
     */
    private boolean createsValidWordsHorizontally(String word, int row, int col) {
        // Check the main word being placed
        if (!wordDictionaryService.isValidWord(word)) {
            return false;
        }
        
        // Check all vertical words that would be created
        for (int i = 0; i < word.length(); i++) {
            if (board.isEmpty(row, col + i)) {
                // This position will have a new tile, check if it creates a valid vertical word
                String verticalWord = getVerticalWordAt(row, col + i, word.charAt(i));
                if (verticalWord.length() > 1 && !wordDictionaryService.isValidWord(verticalWord)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks if placing a word vertically creates valid words in all directions.
     */
    private boolean createsValidWordsVertically(String word, int row, int col) {
        // Check the main word being placed
        if (!wordDictionaryService.isValidWord(word)) {
            return false;
        }
        
        // Check all horizontal words that would be created
        for (int i = 0; i < word.length(); i++) {
            if (board.isEmpty(row + i, col)) {
                // This position will have a new tile, check if it creates a valid horizontal word
                String horizontalWord = getHorizontalWordAt(row + i, col, word.charAt(i));
                if (horizontalWord.length() > 1 && !wordDictionaryService.isValidWord(horizontalWord)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Gets the vertical word that would be formed at a given position.
     */
    private String getVerticalWordAt(int row, int col, char newLetter) {
        StringBuilder word = new StringBuilder();
        
        // Go up to find the start of the word
        int startRow = row;
        while (startRow > 0 && !board.isEmpty(startRow - 1, col)) {
            startRow--;
        }
        
        // Build the word from top to bottom
        for (int r = startRow; r < 15; r++) {
            if (r == row) {
                word.append(newLetter);
            } else if (!board.isEmpty(r, col)) {
                word.append(board.getTile(r, col).getLetter());
            } else {
                break;
            }
        }
        
        return word.toString();
    }
    
    /**
     * Gets the horizontal word that would be formed at a given position.
     */
    private String getHorizontalWordAt(int row, int col, char newLetter) {
        StringBuilder word = new StringBuilder();
        
        // Go left to find the start of the word
        int startCol = col;
        while (startCol > 0 && !board.isEmpty(row, startCol - 1)) {
            startCol--;
        }
        
        // Build the word from left to right
        for (int c = startCol; c < 15; c++) {
            if (c == col) {
                word.append(newLetter);
            } else if (!board.isEmpty(row, c)) {
                word.append(board.getTile(row, c).getLetter());
            } else {
                break;
            }
        }
        
        return word.toString();
    }
    
    /**
     * Helper class to store placement options.
     */
    private static class PlacementOption {
        final int row;
        final int col;
        
        PlacementOption(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
    
    /**
     * Places simple 2-3 letter words to use up remaining tiles.
     */
    private void placeSimpleWords() {
        String[] simpleWords = {"AT", "IT", "IN", "ON", "TO", "GO", "DO", "BE", "HE", "SHE", "THE", "AND", "FOR", "BUT", "NOT", "HAS", "HAD", "WAS", "ARE", "WERE"};
        
        for (String word : simpleWords) {
            if (bag.getRemainingTiles() <= 7) break;
            
            // Try horizontal placement with Scrabble rules
            List<PlacementOption> horizontalPlacements = findValidHorizontalPlacements(word);
            if (!horizontalPlacements.isEmpty()) {
                PlacementOption placement = horizontalPlacements.get(0);
                placeWordHorizontally(word, placement.row, placement.col);
                return;
            }
            
            // Try vertical placement with Scrabble rules
            List<PlacementOption> verticalPlacements = findValidVerticalPlacements(word);
            if (!verticalPlacements.isEmpty()) {
                PlacementOption placement = verticalPlacements.get(0);
                placeWordVertically(word, placement.row, placement.col);
                return;
            }
        }
    }
    
    /**
     * Checks if a word can be placed horizontally at the given position.
     */
    private boolean canPlaceWordHorizontally(String word, int row, int col) {
        if (col + word.length() > 15) {
            return false;
        }
        
        // Check if all positions are empty or match the word
        for (int i = 0; i < word.length(); i++) {
            ScrabbleTile existingTile = board.getTile(row, col + i);
            if (existingTile != null && existingTile.getLetter() != word.charAt(i)) {
                return false;
            }
        }
        
        // Check if we have enough tiles
        return hasEnoughTilesForWord(word);
    }
    
    /**
     * Checks if a word can be placed vertically at the given position.
     */
    private boolean canPlaceWordVertically(String word, int row, int col) {
        if (row + word.length() > 15) {
            return false;
        }
        
        // Check if all positions are empty or match the word
        for (int i = 0; i < word.length(); i++) {
            ScrabbleTile existingTile = board.getTile(row + i, col);
            if (existingTile != null && existingTile.getLetter() != word.charAt(i)) {
                return false;
            }
        }
        
        // Check if we have enough tiles
        return hasEnoughTilesForWord(word);
    }
    
    /**
     * Checks if we have enough tiles to spell the word.
     */
    private boolean hasEnoughTilesForWord(String word) {
        Map<Character, Integer> availableTiles = new HashMap<>();
        List<ScrabbleTile> remainingTiles = bag.getRemainingTilesList();
        
        for (ScrabbleTile tile : remainingTiles) {
            char letter = tile.getLetter();
            availableTiles.put(letter, availableTiles.getOrDefault(letter, 0) + 1);
        }
        
        for (char c : word.toCharArray()) {
            int count = availableTiles.getOrDefault(c, 0);
            if (count == 0) {
                return false;
            }
            availableTiles.put(c, count - 1);
        }
        
        return true;
    }
    
    /**
     * Places a word horizontally on the board.
     */
    private void placeWordHorizontally(String word, int row, int col) {
        for (int i = 0; i < word.length(); i++) {
            if (board.isEmpty(row, col + i)) {
                ScrabbleTile tile = findTileForLetter(word.charAt(i));
                if (tile != null) {
                    board.placeTile(row, col + i, tile);
                    bag.returnTile(tile); // Remove from bag
                }
            }
        }
    }
    
    /**
     * Places a word vertically on the board.
     */
    private void placeWordVertically(String word, int row, int col) {
        for (int i = 0; i < word.length(); i++) {
            if (board.isEmpty(row + i, col)) {
                ScrabbleTile tile = findTileForLetter(word.charAt(i));
                if (tile != null) {
                    board.placeTile(row + i, col, tile);
                    bag.returnTile(tile); // Remove from bag
                }
            }
        }
    }
    
    /**
     * Finds a tile with the specified letter in the bag.
     */
    private ScrabbleTile findTileForLetter(char letter) {
        List<ScrabbleTile> remainingTiles = bag.getRemainingTilesList();
        for (ScrabbleTile tile : remainingTiles) {
            if (tile.getLetter() == letter) {
                return tile;
            }
        }
        return null;
    }
    
    /**
     * Gets a random word from the dictionary.
     */
    private String getRandomWord(int length) {
        return wordDictionaryService.getRandomWord(length);
    }
    
    /**
     * Gets the current board state for the response.
     */
    private Map<String, Object> getBoardState() {
        Map<String, Object> boardState = new HashMap<>();
        boardState.put("size", board.getBoardSize());
        
        List<List<Map<String, Object>>> cells = new ArrayList<>();
        for (int i = 0; i < board.getBoardSize(); i++) {
            List<Map<String, Object>> row = new ArrayList<>();
            for (int j = 0; j < board.getBoardSize(); j++) {
                Map<String, Object> cell = new HashMap<>();
                ScrabbleTile tile = board.getTile(i, j);
                
                if (tile != null) {
                    cell.put("letter", tile.getLetter());
                    cell.put("points", tile.getPointValue());
                    cell.put("hasTile", true);
                } else {
                    cell.put("hasTile", false);
                }
                
                cell.put("wordMultiplier", board.getWordMultiplier(i, j));
                cell.put("letterMultiplier", board.getLetterMultiplier(i, j));
                
                row.add(cell);
            }
            cells.add(row);
        }
        
        boardState.put("cells", cells);
        return boardState;
    }
    
    /**
     * Gets the current puzzle state.
     */
    public Map<String, Object> getCurrentPuzzle() {
        Map<String, Object> puzzle = new HashMap<>();
        puzzle.put("board", getBoardState());
        puzzle.put("remainingTiles", bag.getRemainingTilesList());
        puzzle.put("placedTileCount", board.getPlacedTileCount());
        puzzle.put("remainingTileCount", bag.getRemainingTiles());
        return puzzle;
    }
} 