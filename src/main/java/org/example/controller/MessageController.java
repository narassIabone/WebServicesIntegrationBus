package org.example.controller;

import org.example.service.MessageIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageIngestionService ingestionService;

    public MessageController(MessageIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> request) {
        try {
            String messageId = ingestionService.processInboundMessage(request);
            return ResponseEntity.ok("Message accepted. ID: " + messageId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Processing error: " + e.getMessage());
        }
    }
}