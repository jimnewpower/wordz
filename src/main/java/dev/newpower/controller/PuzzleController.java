package dev.newpower.controller;

import dev.newpower.service.ScrabblePuzzleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/puzzle")
public class PuzzleController {
    
    @Autowired
    private ScrabblePuzzleService puzzleService;
    
    @PostMapping("/generate")
    public Map<String, Object> generatePuzzle() {
        return puzzleService.generatePuzzle();
    }
    
    @GetMapping("/current")
    public Map<String, Object> getCurrentPuzzle() {
        return puzzleService.getCurrentPuzzle();
    }
} 