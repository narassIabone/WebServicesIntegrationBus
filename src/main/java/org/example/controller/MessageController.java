package org.example.controller;

import org.example.model.Message;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @PostMapping
    public String receiveMessage(@RequestBody Map<String, Object> request) {
        // Извлекаем payload (JSON или XML строку)
        String payload = request.get("payload").toString();

        // Извлекаем headers (если есть)
        Map<String, String> headers = (Map<String, String>) request.getOrDefault("headers", Map.of());

        // Создаём объект Message и устанавливаем headers
        Message message = new Message(payload);
        message.setHeaders(headers);

        // Пример: выводим в консоль, пока что без Kafka
        System.out.println("Шина получила новое сообщение:");
        System.out.println("ID: " + message.getId());
        System.out.println("Payload: " + message.getPayload());
        System.out.println("Headers: " + message.getHeaders());
        System.out.println("Status: " + message.getStatus());

        // Можно дальше передать в обработчик
        // messageProcessor.process(message);

        return "Message received successfully with ID: " + message.getId();
    }
}
