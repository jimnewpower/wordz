package dev.newpower.controller;

import dev.newpower.model.ScrabbleBag;
import dev.newpower.model.ScrabbleTile;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scrabble")
public class ScrabbleController {

    private final ScrabbleBag bag = new ScrabbleBag();

    @GetMapping("/bag/info")
    public Map<String, Object> getBagInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("remainingTiles", bag.getRemainingTiles());
        info.put("isEmpty", bag.isEmpty());
        return info;
    }

    @PostMapping("/bag/draw")
    public Map<String, Object> drawTiles(@RequestParam(defaultValue = "7") int count) {
        List<ScrabbleTile> tiles = bag.drawTiles(count);
        
        Map<String, Object> response = new HashMap<>();
        response.put("drawnTiles", tiles.stream().map(ScrabbleTile::getLetter).toList());
        response.put("tileDetails", tiles.stream().map(tile -> {
            Map<String, Object> tileInfo = new HashMap<>();
            tileInfo.put("letter", tile.getLetter());
            tileInfo.put("points", tile.getPointValue());
            return tileInfo;
        }).toList());
        response.put("remainingTiles", bag.getRemainingTiles());
        
        return response;
    }

    @PostMapping("/bag/draw-single")
    public Map<String, Object> drawSingleTile() {
        ScrabbleTile tile = bag.drawTile();
        
        Map<String, Object> response = new HashMap<>();
        if (tile != null) {
            response.put("letter", tile.getLetter());
            response.put("points", tile.getPointValue());
        } else {
            response.put("message", "Bag is empty");
        }
        response.put("remainingTiles", bag.getRemainingTiles());
        
        return response;
    }

    @PostMapping("/bag/reset")
    public Map<String, Object> resetBag() {
        bag.reset();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bag reset successfully");
        response.put("remainingTiles", bag.getRemainingTiles());
        
        return response;
    }

    @GetMapping("/bag/shuffle")
    public Map<String, Object> shuffleBag() {
        bag.shuffle();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bag shuffled successfully");
        response.put("remainingTiles", bag.getRemainingTiles());
        
        return response;
    }

    @GetMapping("/points/{letter}")
    public Map<String, Object> getLetterPoints(@PathVariable char letter) {
        int points = ScrabbleBag.getPointValue(letter);
        int count = ScrabbleBag.getLetterCount(letter);
        
        Map<String, Object> response = new HashMap<>();
        response.put("letter", letter);
        response.put("points", points);
        response.put("count", count);
        
        return response;
    }

    @GetMapping("/distribution")
    public Map<String, Object> getFullDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        // Add point values
        Map<Character, Integer> points = new HashMap<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            points.put(c, ScrabbleBag.getPointValue(c));
        }
        distribution.put("pointValues", points);
        
        // Add letter counts
        Map<Character, Integer> counts = new HashMap<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            counts.put(c, ScrabbleBag.getLetterCount(c));
        }
        distribution.put("letterCounts", counts);
        
        return distribution;
    }
} 