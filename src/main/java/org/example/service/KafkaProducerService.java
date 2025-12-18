package org.example.service;

import org.example.model.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// Сервис для отправки сообщений между узлами маршрута через Kafka
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Отправляет сообщение в указанный топик.
     * @param message - сообщение для отправки.
     * @param nextTopic - имя следующего топика в конвейере.
     */
    public void route(Message message, String nextTopic) {
        String key = message.getId();

        System.out.println("-> Publishing message ID: " + message.getId() +
                " to next topic: " + nextTopic);

        kafkaTemplate.send(nextTopic, key, message);
    }
}