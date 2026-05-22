package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.entity.Message;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final KafkaAdminService kafkaAdminService;

    public void route(Message message, String topic) {
        kafkaAdminService.ensureTopicExists(topic);

        try {
            log.info("[Kafka] Отправка сообщения {} в топик: {}", message.getId(), topic);
            kafkaTemplate.send(topic, message);
        } catch (Exception e) {
            log.error("[Error] Ошибка при отправке сообщения {} в Kafka (топик: {}). Причина: {}",
                    message.getId(), topic, e.getMessage());
            throw e;
        }
    }
}