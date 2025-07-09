package dev.newpower.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ScrabbleBagTest {

    private ScrabbleBag bag;

    @BeforeEach
    void setUp() {
        bag = new ScrabbleBag();
    }

    @Test
    void testInitialBagSize() {
        assertEquals(100, bag.getRemainingTiles(), "Bag should start with exactly 100 tiles");
    }

    @Test
    void testDrawSingleTile() {
        ScrabbleTile tile = bag.drawTile();
        assertNotNull(tile, "Should be able to draw a tile from a full bag");
        assertEquals(99, bag.getRemainingTiles(), "Bag should have 99 tiles after drawing one");
    }

    @Test
    void testDrawMultipleTiles() {
        List<ScrabbleTile> tiles = bag.drawTiles(7);
        assertEquals(7, tiles.size(), "Should draw exactly 7 tiles");
        assertEquals(93, bag.getRemainingTiles(), "Bag should have 93 tiles after drawing 7");
    }

    @Test
    void testReturnTiles() {
        List<ScrabbleTile> drawnTiles = bag.drawTiles(5);
        int initialCount = bag.getRemainingTiles();
        
        bag.returnTiles(drawnTiles);
        assertEquals(initialCount + 5, bag.getRemainingTiles(), "Bag should have 5 more tiles after returning them");
    }

    @Test
    void testEmptyBag() {
        // Draw all tiles
        List<ScrabbleTile> allTiles = bag.drawTiles(100);
        assertEquals(100, allTiles.size(), "Should be able to draw all 100 tiles");
        assertTrue(bag.isEmpty(), "Bag should be empty after drawing all tiles");
        
        // Try to draw from empty bag
        ScrabbleTile tile = bag.drawTile();
        assertNull(tile, "Should return null when drawing from empty bag");
    }

    @Test
    void testPointValues() {
        assertEquals(1, ScrabbleBag.getPointValue('A'), "A should be worth 1 point");
        assertEquals(3, ScrabbleBag.getPointValue('B'), "B should be worth 3 points");
        assertEquals(10, ScrabbleBag.getPointValue('Q'), "Q should be worth 10 points");
        assertEquals(0, ScrabbleBag.getPointValue(' '), "Blank tiles should be worth 0 points");
        assertEquals(0, ScrabbleBag.getPointValue('!'), "Invalid character should be worth 0 points");
    }

    @Test
    void testLetterCounts() {
        assertEquals(9, ScrabbleBag.getLetterCount('A'), "Should have 9 A tiles");
        assertEquals(12, ScrabbleBag.getLetterCount('E'), "Should have 12 E tiles");
        assertEquals(1, ScrabbleBag.getLetterCount('Q'), "Should have 1 Q tile");
        assertEquals(2, ScrabbleBag.getLetterCount(' '), "Should have 2 blank tiles");
        assertEquals(0, ScrabbleBag.getLetterCount('!'), "Invalid character should have 0 count");
    }

    @Test
    void testTileDistribution() {
        // Draw all tiles and count them
        List<ScrabbleTile> allTiles = bag.drawTiles(100);
        Map<Character, Long> letterCounts = allTiles.stream()
                .collect(Collectors.groupingBy(ScrabbleTile::getLetter, Collectors.counting()));
        
        // Verify some key distributions
        assertEquals(9, letterCounts.get('A'), "Should have exactly 9 A tiles");
        assertEquals(12, letterCounts.get('E'), "Should have exactly 12 E tiles");
        assertEquals(1, letterCounts.get('Q'), "Should have exactly 1 Q tile");
        assertEquals(1, letterCounts.get('Z'), "Should have exactly 1 Z tile");
        assertEquals(2, letterCounts.get(' '), "Should have exactly 2 blank tiles");
    }

    @Test
    void testShuffle() {
        List<ScrabbleTile> beforeShuffle = bag.getRemainingTilesList();
        bag.shuffle();
        List<ScrabbleTile> afterShuffle = bag.getRemainingTilesList();
        
        assertEquals(100, afterShuffle.size(), "Shuffle should not change the number of tiles");
        // Note: In rare cases, shuffle might produce the same order, so we can't reliably test for different order
    }

    @Test
    void testReset() {
        bag.drawTiles(50);
        assertEquals(50, bag.getRemainingTiles(), "Should have 50 tiles after drawing half");
        
        bag.reset();
        assertEquals(100, bag.getRemainingTiles(), "Should have 100 tiles after reset");
    }

    @Test
    void testDrawMoreThanAvailable() {
        bag.drawTiles(95); // Draw most tiles
        List<ScrabbleTile> remainingTiles = bag.drawTiles(10); // Try to draw more than available
        
        assertEquals(5, remainingTiles.size(), "Should only draw the remaining 5 tiles");
        assertTrue(bag.isEmpty(), "Bag should be empty after drawing all available tiles");
    }
} 