package dev.newpower.controller;

import dev.newpower.service.ScrabblePuzzleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/puzzle")
public class PuzzleController {
    
    @Autowired
    private ScrabblePuzzleService puzzleService;
    
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @PostMapping("/generate")
    public Map<String, Object> generatePuzzle() {
        return puzzleService.generatePuzzle();
    }
    
    @GetMapping(value = "/generate-animated", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generatePuzzleAnimated() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        
        // Remove emitter when client disconnects
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));
        
        // Run puzzle generation asynchronously
        executorService.submit(() -> {
            try {
                // Send initial event
                emitter.send(SseEmitter.event()
                    .name("generation_started")
                    .data("Puzzle generation started"));
                
                // Generate puzzle with placement callback
                Map<String, Object> puzzle = puzzleService.generatePuzzle(placementEvent -> {
                    try {
                        String eventType = (String) placementEvent.get("type");
                        if ("word_complete".equals(eventType)) {
                            emitter.send(SseEmitter.event()
                                .name("word_complete")
                                .data(placementEvent));
                        } else if ("progress_update".equals(eventType)) {
                            emitter.send(SseEmitter.event()
                                .name("progress_update")
                                .data(placementEvent));
                        } else if ("delay".equals(eventType)) {
                            emitter.send(SseEmitter.event()
                                .name("delay")
                                .data(placementEvent));
                        } else {
                            emitter.send(SseEmitter.event()
                                .name("tile_placed")
                                .data(placementEvent));
                        }
                    } catch (IOException e) {
                        emitters.remove(emitter);
                    }
                });
                
                // Send final puzzle data
                emitter.send(SseEmitter.event()
                    .name("generation_complete")
                    .data(puzzle));
                
                // Complete the emitter
                emitter.complete();
                
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
    
    @GetMapping("/current")
    public Map<String, Object> getCurrentPuzzle() {
        return puzzleService.getCurrentPuzzle();
    }
} 