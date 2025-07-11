package dev.newpower.model;

import java.util.*;

/**
 * Represents a 15x15 Scrabble board with word placement validation and scoring.
 */
public class ScrabbleBoard {
    private final ScrabbleTile[][] board;
    private final int BOARD_SIZE = 15;
    private final int CENTER_ROW = 7;
    private final int CENTER_COL = 7;
    
    // Special cell multipliers
    private final int[][] wordMultipliers;
    private final int[][] letterMultipliers;
    
    public ScrabbleBoard() {
        this.board = new ScrabbleTile[BOARD_SIZE][BOARD_SIZE];
        this.wordMultipliers = new int[BOARD_SIZE][BOARD_SIZE];
        this.letterMultipliers = new int[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }
    
    /**
     * Initializes the board with special cell multipliers.
     */
    private void initializeBoard() {
        // Initialize all multipliers to 1
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(wordMultipliers[i], 1);
            Arrays.fill(letterMultipliers[i], 1);
        }
        
        // Triple Word (corners and center edges)
        int[][] tripleWordPositions = {
            {0, 0}, {0, 7}, {0, 14},
            {7, 0}, {7, 14},
            {14, 0}, {14, 7}, {14, 14}
        };
        
        // Double Word (diagonal lines from corners and center star)
        int[][] doubleWordPositions = {
            {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {8, 8}, {9, 9}, {10, 10}, {11, 11}, {12, 12}, {13, 13},
            {1, 13}, {2, 12}, {3, 11}, {4, 10}, {5, 9}, {6, 8}, {8, 6}, {9, 5}, {10, 4}, {11, 3}, {12, 2}, {13, 1}
        };
        
        // Triple Letter
        int[][] tripleLetterPositions = {
            {1, 5}, {1, 9},
            {5, 1}, {5, 5}, {5, 9}, {5, 13},
            {9, 1}, {9, 5}, {9, 9}, {9, 13},
            {13, 5}, {13, 9}
        };
        
        // Double Letter
        int[][] doubleLetterPositions = {
            {0, 3}, {0, 11},
            {2, 6}, {2, 8},
            {3, 0}, {3, 7}, {3, 14},
            {6, 2}, {6, 6}, {6, 8}, {6, 12},
            {7, 3}, {7, 11},
            {8, 2}, {8, 6}, {8, 8}, {8, 12},
            {11, 0}, {11, 7}, {11, 14},
            {12, 6}, {12, 8},
            {14, 3}, {14, 11}
        };
        
        // Set multipliers
        for (int[] pos : tripleWordPositions) {
            wordMultipliers[pos[0]][pos[1]] = 3;
        }
        
        for (int[] pos : doubleWordPositions) {
            wordMultipliers[pos[0]][pos[1]] = 2;
        }
        
        for (int[] pos : tripleLetterPositions) {
            letterMultipliers[pos[0]][pos[1]] = 3;
        }
        
        for (int[] pos : doubleLetterPositions) {
            letterMultipliers[pos[0]][pos[1]] = 2;
        }
    }
    
    /**
     * Places a tile at the specified position.
     */
    public void placeTile(int row, int col, ScrabbleTile tile) {
        if (isValidPosition(row, col)) {
            board[row][col] = tile;
        }
    }
    
    /**
     * Gets a tile at the specified position.
     */
    public ScrabbleTile getTile(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }
    
    /**
     * Checks if a position is valid on the board.
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }
    
    /**
     * Checks if a position is empty.
     */
    public boolean isEmpty(int row, int col) {
        return isValidPosition(row, col) && board[row][col] == null;
    }
    
    /**
     * Gets the word multiplier at a position.
     */
    public int getWordMultiplier(int row, int col) {
        return isValidPosition(row, col) ? wordMultipliers[row][col] : 1;
    }
    
    /**
     * Gets the letter multiplier at a position.
     */
    public int getLetterMultiplier(int row, int col) {
        return isValidPosition(row, col) ? letterMultipliers[row][col] : 1;
    }
    
    /**
     * Checks if the center position is occupied (required for first word).
     */
    public boolean isCenterOccupied() {
        return board[CENTER_ROW][CENTER_COL] != null;
    }
    
    /**
     * Gets the board size.
     */
    public int getBoardSize() {
        return BOARD_SIZE;
    }
    
    /**
     * Gets the center position.
     */
    public int getCenterRow() {
        return CENTER_ROW;
    }
    
    public int getCenterCol() {
        return CENTER_COL;
    }
    
    /**
     * Clears the board.
     */
    public void clear() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], null);
        }
    }
    
    /**
     * Gets all placed tiles on the board.
     */
    public List<ScrabbleTile> getPlacedTiles() {
        List<ScrabbleTile> tiles = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null) {
                    tiles.add(board[i][j]);
                }
            }
        }
        return tiles;
    }
    
    /**
     * Gets the number of placed tiles.
     */
    public int getPlacedTileCount() {
        return getPlacedTiles().size();
    }
} 