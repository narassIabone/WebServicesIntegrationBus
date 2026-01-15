package org.example.controller;

import org.example.model.core.Message;
import org.example.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> request) {
        // звлекаем payload
        Object payloadObj = request.get("payload");
        if (payloadObj == null) {
            return ResponseEntity.badRequest().body("Error: 'payload' is required");
        }

        // Извлекаем headers
        Map<String, Object> headers = (Map<String, Object>) request.get("headers");

        Message message = new Message(payloadObj.toString());
        if (headers != null) {
            message.getHeaders().putAll(headers);
        }

        // Вывод в консоль

        System.out.println("=== Шина получила новое сообщение ===");
        System.out.println("ID: " + message.getId());
        System.out.println("Time (UTC): " + message.getCreatedAt());
        System.out.println("Status: " + message.getStatus());

        // Сохранение в БД
        try {
            messageService.saveMessage(message);

            System.out.println("Сообщение успешно сохранено в БД с ID: " + message.getId());

            return ResponseEntity.ok("Message received and saved. ID: " + message.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to save message: " + e.getMessage());
        }
    }
}