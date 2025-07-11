package dev.newpower.service;

import dev.newpower.model.ScrabbleBag;
import dev.newpower.model.ScrabbleBoard;
import dev.newpower.model.ScrabbleTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

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
     * and returns 7 random tiles from the remaining tiles as the puzzle.
     */
    public Map<String, Object> generatePuzzle() {
        return generatePuzzle(null);
    }
    
    /**
     * Generates a new Scrabble puzzle with placement events.
     */
    public Map<String, Object> generatePuzzle(Consumer<Map<String, Object>> placementCallback) {
        // Reset everything
        bag.reset();
        board.clear();
        
        // Send initial progress event
        if (placementCallback != null) {
            Map<String, Object> progressEvent = new HashMap<>();
            progressEvent.put("type", "progress_update");
            progressEvent.put("tilesPlaced", 0);
            progressEvent.put("totalTiles", 93);
            progressEvent.put("percentage", 0);
            placementCallback.accept(progressEvent);
        }
        
        // Place words on the board
        placeWordsOnBoard(placementCallback);
        
        // Get all remaining tiles and select 7 random ones
        List<ScrabbleTile> allRemainingTiles = bag.getRemainingTilesList();
        List<ScrabbleTile> selectedTiles = selectRandomTiles(allRemainingTiles, 7);
        
        // Create response
        Map<String, Object> puzzle = new HashMap<>();
        puzzle.put("board", getBoardState());
        puzzle.put("remainingTiles", selectedTiles);
        puzzle.put("placedTileCount", board.getPlacedTileCount());
        puzzle.put("remainingTileCount", allRemainingTiles.size());
        
        return puzzle;
    }
    
    /**
     * Places valid words on the board using tiles from the bag.
     */
    private void placeWordsOnBoard(Consumer<Map<String, Object>> placementCallback) {
        // Start with a word that goes through the center
        String firstWord = getRandomWord(5);
        placeWordHorizontally(firstWord, 7, 5, placementCallback); // Center row, starting at column 5
        
        // Send word completion event
        if (placementCallback != null) {
            Map<String, Object> wordCompleteEvent = new HashMap<>();
            wordCompleteEvent.put("type", "word_complete");
            wordCompleteEvent.put("word", firstWord);
            wordCompleteEvent.put("direction", "horizontal");
            wordCompleteEvent.put("row", 7);
            wordCompleteEvent.put("col", 5);
            placementCallback.accept(wordCompleteEvent);
        }
        
        // Place additional words with gameplay simulation
        placeAdditionalWordsWithGameplay(placementCallback);
    }
    
    /**
     * Places additional words on the board simulating actual Scrabble gameplay.
     * Only places tiles one at a time, ensuring all words are connected to existing tiles.
     */
    private void placeAdditionalWordsWithGameplay(Consumer<Map<String, Object>> placementCallback) {
        int attempts = 0;
        int maxAttempts = 300; // Increased for more attempts since we're more selective
        
        while (board.getPlacedTileCount() < 80 && attempts < maxAttempts && bag.getRemainingTiles() > 15) {
            attempts++;
            
            // Always try to place a word that connects to existing tiles (Scrabble rule)
            if (placeConnectingWord(placementCallback)) {
                // Word was placed successfully, send a delay event
                if (placementCallback != null) {
                    Map<String, Object> delayEvent = new HashMap<>();
                    delayEvent.put("type", "delay");
                    delayEvent.put("duration", 300);
                    placementCallback.accept(delayEvent);
                }
            }
        }
        
        // If we still have too many tiles, place some very simple words that connect
        while (bag.getRemainingTiles() > 7 && attempts < maxAttempts * 2) {
            attempts++;
            if (placeVerySimpleConnectingWords(placementCallback)) {
                // Word was placed successfully, send a delay event
                if (placementCallback != null) {
                    Map<String, Object> delayEvent = new HashMap<>();
                    delayEvent.put("type", "delay");
                    delayEvent.put("duration", 200);
                    placementCallback.accept(delayEvent);
                }
            }
        }
    }
    
    /**
     * Places a word that connects to existing tiles on the board (like real Scrabble).
     * Returns true if a word was placed, false otherwise.
     */
    private boolean placeConnectingWord(Consumer<Map<String, Object>> placementCallback) {
        // Try to find a word that can connect to existing tiles
        String word = getRandomWord(3 + (int)(Math.random() * 4)); // 3-6 letters
        
        // Try to find a valid placement that uses existing tiles
        List<PlacementOption> horizontalPlacements = findValidHorizontalPlacements(word);
        List<PlacementOption> verticalPlacements = findValidVerticalPlacements(word);
        
        // Combine all valid placements
        List<PlacementOption> allValidPlacements = new ArrayList<>();
        allValidPlacements.addAll(horizontalPlacements);
        allValidPlacements.addAll(verticalPlacements);
        
        if (!allValidPlacements.isEmpty()) {
            // Choose a random valid placement
            PlacementOption placement = allValidPlacements.get((int)(Math.random() * allValidPlacements.size()));
            
            // Determine if it's horizontal or vertical placement
            boolean isHorizontal = horizontalPlacements.contains(placement);
            
            if (isHorizontal) {
                placeWordHorizontally(word, placement.row, placement.col, placementCallback);
            } else {
                placeWordVertically(word, placement.row, placement.col, placementCallback);
            }
            
            // Send word completion event
            if (placementCallback != null) {
                Map<String, Object> wordCompleteEvent = new HashMap<>();
                wordCompleteEvent.put("type", "word_complete");
                wordCompleteEvent.put("word", word);
                wordCompleteEvent.put("direction", isHorizontal ? "horizontal" : "vertical");
                wordCompleteEvent.put("row", placement.row);
                wordCompleteEvent.put("col", placement.col);
                placementCallback.accept(wordCompleteEvent);
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Places very simple 2-3 letter words that connect to existing tiles.
     * Returns true if a word was placed, false otherwise.
     */
    private boolean placeVerySimpleConnectingWords(Consumer<Map<String, Object>> placementCallback) {
        String[] simpleWords = {"AT", "IT", "IN", "ON", "TO", "GO", "DO", "BE", "HE", "SHE", "THE", "AND", "FOR", "BUT", "NOT", "HAS", "HAD", "WAS", "ARE", "WERE"};
        
        for (String word : simpleWords) {
            if (bag.getRemainingTiles() <= 7) break;
            
            // Try horizontal placement that connects to existing tiles
            List<PlacementOption> horizontalPlacements = findValidHorizontalPlacements(word);
            if (!horizontalPlacements.isEmpty()) {
                PlacementOption placement = horizontalPlacements.get(0);
                placeWordHorizontally(word, placement.row, placement.col, placementCallback);
                
                // Send word completion event
                if (placementCallback != null) {
                    Map<String, Object> wordCompleteEvent = new HashMap<>();
                    wordCompleteEvent.put("type", "word_complete");
                    wordCompleteEvent.put("word", word);
                    wordCompleteEvent.put("direction", "horizontal");
                    wordCompleteEvent.put("row", placement.row);
                    wordCompleteEvent.put("col", placement.col);
                    placementCallback.accept(wordCompleteEvent);
                }
                
                return true;
            }
            
            // Try vertical placement that connects to existing tiles
            List<PlacementOption> verticalPlacements = findValidVerticalPlacements(word);
            if (!verticalPlacements.isEmpty()) {
                PlacementOption placement = verticalPlacements.get(0);
                placeWordVertically(word, placement.row, placement.col, placementCallback);
                
                // Send word completion event
                if (placementCallback != null) {
                    Map<String, Object> wordCompleteEvent = new HashMap<>();
                    wordCompleteEvent.put("type", "word_complete");
                    wordCompleteEvent.put("word", word);
                    wordCompleteEvent.put("direction", "vertical");
                    wordCompleteEvent.put("row", placement.row);
                    wordCompleteEvent.put("col", placement.col);
                    placementCallback.accept(wordCompleteEvent);
                }
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Finds all valid horizontal placements for a word that use existing tiles.
     */
    private List<PlacementOption> findValidHorizontalPlacements(String word) {
        List<PlacementOption> validPlacements = new ArrayList<>();
        
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col <= 15 - word.length(); col++) {
                if (canPlaceWordHorizontally(word, row, col) && 
                    isTrulyConnectedHorizontally(word, row, col) &&
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
                    isTrulyConnectedVertically(word, row, col) &&
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
        // Check the complete horizontal word that would be formed (including extensions)
        String completeHorizontalWord = getCompleteHorizontalWord(word, row, col);
        if (completeHorizontalWord.length() > 1 && !wordDictionaryService.isValidWord(completeHorizontalWord)) {
            System.out.println("Invalid horizontal word formed: " + completeHorizontalWord + " from placing " + word + " at (" + row + "," + col + ")");
            return false;
        }
        
        // Check all vertical words that would be created
        for (int i = 0; i < word.length(); i++) {
            if (board.isEmpty(row, col + i)) {
                // This position will have a new tile, check if it creates a valid vertical word
                String verticalWord = getVerticalWordAt(row, col + i, word.charAt(i));
                if (verticalWord.length() > 1 && !wordDictionaryService.isValidWord(verticalWord)) {
                    System.out.println("Invalid vertical word formed: " + verticalWord + " from placing " + word + " at (" + row + "," + col + ")");
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
        // Check the complete vertical word that would be formed (including extensions)
        String completeVerticalWord = getCompleteVerticalWord(word, row, col);
        if (completeVerticalWord.length() > 1 && !wordDictionaryService.isValidWord(completeVerticalWord)) {
            System.out.println("Invalid vertical word formed: " + completeVerticalWord + " from placing " + word + " at (" + row + "," + col + ")");
            return false;
        }
        
        // Check all horizontal words that would be created
        for (int i = 0; i < word.length(); i++) {
            if (board.isEmpty(row + i, col)) {
                // This position will have a new tile, check if it creates a valid horizontal word
                String horizontalWord = getHorizontalWordAt(row + i, col, word.charAt(i));
                if (horizontalWord.length() > 1 && !wordDictionaryService.isValidWord(horizontalWord)) {
                    System.out.println("Invalid horizontal word formed: " + horizontalWord + " from placing " + word + " at (" + row + "," + col + ")");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks if a horizontal word placement is truly connected to existing tiles.
     * A word is truly connected if it either:
     * 1. Uses an existing tile in its placement, OR
     * 2. Creates a valid word that extends an existing word
     */
    private boolean isTrulyConnectedHorizontally(String word, int row, int col) {
        // Check if the word uses any existing tiles
        boolean usesExistingTile = false;
        for (int i = 0; i < word.length(); i++) {
            if (!board.isEmpty(row, col + i)) {
                usesExistingTile = true;
                break;
            }
        }
        
        if (usesExistingTile) {
            return true;
        }
        
        // Check if the word extends an existing word horizontally
        String completeHorizontalWord = getCompleteHorizontalWord(word, row, col);
        if (completeHorizontalWord.length() > word.length()) {
            return true;
        }
        
        // Check if the word creates a valid vertical word that extends an existing word
        for (int i = 0; i < word.length(); i++) {
            String verticalWord = getVerticalWordAt(row, col + i, word.charAt(i));
            if (verticalWord.length() > 1) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a vertical word placement is truly connected to existing tiles.
     * A word is truly connected if it either:
     * 1. Uses an existing tile in its placement, OR
     * 2. Creates a valid word that extends an existing word
     */
    private boolean isTrulyConnectedVertically(String word, int row, int col) {
        // Check if the word uses any existing tiles
        boolean usesExistingTile = false;
        for (int i = 0; i < word.length(); i++) {
            if (!board.isEmpty(row + i, col)) {
                usesExistingTile = true;
                break;
            }
        }
        
        if (usesExistingTile) {
            return true;
        }
        
        // Check if the word extends an existing word vertically
        String completeVerticalWord = getCompleteVerticalWord(word, row, col);
        if (completeVerticalWord.length() > word.length()) {
            return true;
        }
        
        // Check if the word creates a valid horizontal word that extends an existing word
        for (int i = 0; i < word.length(); i++) {
            String horizontalWord = getHorizontalWordAt(row + i, col, word.charAt(i));
            if (horizontalWord.length() > 1) {
                return true;
            }
        }
        
        return false;
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
     * Gets the complete horizontal word that would be formed when placing a word at the given position.
     * This includes any existing tiles that the new word would extend.
     */
    private String getCompleteHorizontalWord(String word, int row, int col) {
        StringBuilder completeWord = new StringBuilder();
        
        // Go left to find the start of any existing word
        int startCol = col;
        while (startCol > 0 && !board.isEmpty(row, startCol - 1)) {
            startCol--;
        }
        
        // Build the complete word from left to right
        for (int c = startCol; c < 15; c++) {
            if (c >= col && c < col + word.length()) {
                // This is where our new word goes
                completeWord.append(word.charAt(c - col));
            } else if (!board.isEmpty(row, c)) {
                // This is an existing tile
                completeWord.append(board.getTile(row, c).getLetter());
            } else {
                break;
            }
        }
        
        return completeWord.toString();
    }

    /**
     * Gets the complete vertical word that would be formed when placing a word at the given position.
     * This includes any existing tiles that the new word would extend.
     */
    private String getCompleteVerticalWord(String word, int row, int col) {
        StringBuilder completeWord = new StringBuilder();
        
        // Go up to find the start of any existing word
        int startRow = row;
        while (startRow > 0 && !board.isEmpty(startRow - 1, col)) {
            startRow--;
        }
        
        // Build the complete word from top to bottom
        for (int r = startRow; r < 15; r++) {
            if (r >= row && r < row + word.length()) {
                // This is where our new word goes
                completeWord.append(word.charAt(r - row));
            } else if (!board.isEmpty(r, col)) {
                // This is an existing tile
                completeWord.append(board.getTile(r, col).getLetter());
            } else {
                break;
            }
        }
        
        return completeWord.toString();
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
    private void placeWordHorizontally(String word, int row, int col, Consumer<Map<String, Object>> placementCallback) {
        System.out.println("Placing word horizontally: " + word + " at (" + row + "," + col + ")");
        for (int i = 0; i < word.length(); i++) {
            if (board.isEmpty(row, col + i)) {
                ScrabbleTile tile = findTileForLetter(word.charAt(i));
                if (tile != null) {
                    board.placeTile(row, col + i, tile);
                    
                    // Emit placement event if callback is provided
                    if (placementCallback != null) {
                        Map<String, Object> placementEvent = new HashMap<>();
                        placementEvent.put("type", "tile_placed");
                        placementEvent.put("row", row);
                        placementEvent.put("col", col + i);
                        placementEvent.put("letter", tile.getLetter());
                        placementEvent.put("points", tile.getPointValue());
                        placementEvent.put("word", word);
                        placementEvent.put("direction", "horizontal");
                        placementEvent.put("position", i);
                        placementEvent.put("totalTiles", word.length());
                        placementEvent.put("letterMultiplier", board.getLetterMultiplier(row, col + i));
                        placementEvent.put("wordMultiplier", board.getWordMultiplier(row, col + i));
                        
                        placementCallback.accept(placementEvent);
                        
                        // Send progress update
                        Map<String, Object> progressEvent = new HashMap<>();
                        progressEvent.put("type", "progress_update");
                        progressEvent.put("tilesPlaced", board.getPlacedTileCount());
                        progressEvent.put("totalTiles", 93);
                        progressEvent.put("percentage", Math.round((board.getPlacedTileCount() / 93.0) * 100));
                        placementCallback.accept(progressEvent);
                    }
                }
            }
        }
    }
    
    /**
     * Places a word vertically on the board.
     */
    private void placeWordVertically(String word, int row, int col, Consumer<Map<String, Object>> placementCallback) {
        System.out.println("Placing word vertically: " + word + " at (" + row + "," + col + ")");
        for (int i = 0; i < word.length(); i++) {
            if (board.isEmpty(row + i, col)) {
                ScrabbleTile tile = findTileForLetter(word.charAt(i));
                if (tile != null) {
                    board.placeTile(row + i, col, tile);
                    
                    // Emit placement event if callback is provided
                    if (placementCallback != null) {
                        Map<String, Object> placementEvent = new HashMap<>();
                        placementEvent.put("type", "tile_placed");
                        placementEvent.put("row", row + i);
                        placementEvent.put("col", col);
                        placementEvent.put("letter", tile.getLetter());
                        placementEvent.put("points", tile.getPointValue());
                        placementEvent.put("word", word);
                        placementEvent.put("direction", "vertical");
                        placementEvent.put("position", i);
                        placementEvent.put("totalTiles", word.length());
                        placementEvent.put("letterMultiplier", board.getLetterMultiplier(row + i, col));
                        placementEvent.put("wordMultiplier", board.getWordMultiplier(row + i, col));
                        
                        placementCallback.accept(placementEvent);
                        
                        // Send progress update
                        Map<String, Object> progressEvent = new HashMap<>();
                        progressEvent.put("type", "progress_update");
                        progressEvent.put("tilesPlaced", board.getPlacedTileCount());
                        progressEvent.put("totalTiles", 93);
                        progressEvent.put("percentage", Math.round((board.getPlacedTileCount() / 93.0) * 100));
                        placementCallback.accept(progressEvent);
                    }
                }
            }
        }
    }
    
    /**
     * Finds and removes a tile with the specified letter from the bag.
     */
    private ScrabbleTile findTileForLetter(char letter) {
        return bag.removeTileWithLetter(letter);
    }
    
    /**
     * Gets a random word from the dictionary.
     */
    private String getRandomWord(int length) {
        return wordDictionaryService.getRandomWord(length);
    }

    /**
     * Selects a random subset of tiles from the given list.
     * @param tiles The list of tiles to select from
     * @param count The number of tiles to select
     * @return A list of randomly selected tiles
     */
    private List<ScrabbleTile> selectRandomTiles(List<ScrabbleTile> tiles, int count) {
        List<ScrabbleTile> copy = new ArrayList<>(tiles);
        List<ScrabbleTile> selected = new ArrayList<>();
        Random random = new Random();
        
        int tilesToSelect = Math.min(count, copy.size());
        for (int i = 0; i < tilesToSelect; i++) {
            int index = random.nextInt(copy.size());
            selected.add(copy.remove(index));
        }
        
        return selected;
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
        // Get all remaining tiles and select 7 random ones
        List<ScrabbleTile> allRemainingTiles = bag.getRemainingTilesList();
        List<ScrabbleTile> selectedTiles = selectRandomTiles(allRemainingTiles, 7);
        
        Map<String, Object> puzzle = new HashMap<>();
        puzzle.put("board", getBoardState());
        puzzle.put("remainingTiles", selectedTiles);
        puzzle.put("placedTileCount", board.getPlacedTileCount());
        puzzle.put("remainingTileCount", allRemainingTiles.size());
        return puzzle;
    }
} 