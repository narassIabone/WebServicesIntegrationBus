package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.KafkaProducerService;
import org.example.model.core.Message;
import org.example.model.core.MessageStatus;
import org.example.model.route.RouteConfig;
import org.example.repository.MessageRepository;
import org.example.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.time.Instant;

@Slf4j
@Service
public class MessageIngestionService {

    @Value("${app.kafka.topics.inbound:messages.new}")
    private String inboundTopic;

    private final MessageRepository messageRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RouteRepository routeRepository;

    public MessageIngestionService(MessageRepository messageRepository,
                                   RouteRepository routeRepository,
                                   KafkaProducerService kafkaProducerService) {
        this.messageRepository = messageRepository;
        this.routeRepository = routeRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public String processInboundMessage(Map<String, Object> request) {
        String routeName = (String) request.get("routeName");
        Object payloadObj = request.get("payload");

        log.info("[Ingestion] Получен новый запрос на маршрут: {}", routeName);

        if (payloadObj == null || routeName == null) {
            log.warn("[Error] Некорректный запрос: отсутствует payload или routeName");
            throw new IllegalArgumentException("Payload and routeName are required");
        }

        RouteConfig route = routeRepository.findByName(routeName)
                .orElseThrow(() -> {
                    log.error("[Error] Маршрут '{}' не зарегистрирован в системе", routeName);
                    return new IllegalArgumentException("Route not found: " + routeName);
                });

        Message message = Message.builder()
                .id(UUID.randomUUID())
                .routeId(route.getId())
                .payload(payloadObj.toString())
                .status(MessageStatus.NEW)
                .createdAt(Instant.now())
                .headers(new HashMap<>())
                .context(new HashMap<>())
                .build();

        Map<String, Object> headers = (Map<String, Object>) request.get("headers");
        if (headers != null) {
            message.getHeaders().putAll(headers);
        }

        // 4. Сохранение и отправка
        messageRepository.save(message);
        log.info("[Ingestion] Сообщение {} успешно сохранено в БД. Отправка в топик {}", message.getId(), inboundTopic);

        try {
            kafkaProducerService.route(message, inboundTopic);
        } catch (Exception e) {
            log.error("[Fatal] Сообщение {} сохранено в БД, но произошел сбой при отправке в Kafka: {}",
                    message.getId(), e.getMessage());
            throw e;
        }

        return message.getId().toString();
    }
}