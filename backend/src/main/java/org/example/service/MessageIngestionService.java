package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.config.KafkaProducerService;
import org.example.model.entity.Message;
import org.example.model.enums.MessageStatus;
import org.example.model.entity.RouteConfig;
import org.example.repository.MessageRepository;
import org.example.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.time.Instant;

@Slf4j
@Service
public class MessageIngestionService {

    private final MessageRepository messageRepository;
    private final RouteRepository routeRepository;
    private final KafkaProducerService kafkaProducerService;
    private final SystemSettingsService settingsService;
    private final ObjectMapper objectMapper;

    public MessageIngestionService(MessageRepository messageRepository,
                                   RouteRepository routeRepository,
                                   KafkaProducerService kafkaProducerService,
                                   SystemSettingsService settingsService,
                                   ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.routeRepository = routeRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
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

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(payloadObj);
        } catch (Exception e) {
            log.error("[Ingestion] Ошибка конвертации payload в JSON", e);
            throw new RuntimeException("Failed to serialize payload to JSON", e);
        }

        Map<String, Object> headers = (Map<String, Object>) request.get("headers");
        Optional<RouteConfig> routeOpt = routeRepository.findByName(routeName);

        if (routeOpt.isEmpty()) {
            String unknownTopic = settingsService.getString("topic_unknown", "messages.unknown");
            log.error("[Error] Маршрут '{}' не зарегистрирован. Отправка сообщения в топик: {}", routeName, unknownTopic);

            Message unknownMessage = Message.builder()
                    .id(UUID.randomUUID())
                    .routeId(null)
                    .payload(jsonPayload)
                    .status(MessageStatus.NEW)
                    .createdAt(Instant.now())
                    .headers(headers != null ? new HashMap<>(headers) : new HashMap<>())
                    .context(new HashMap<>())
                    .build();

            unknownMessage.getContext().put("error_reason", "Unregistered routeName: " + routeName);

            try {
                kafkaProducerService.route(unknownMessage, unknownTopic);
            } catch (Exception e) {
                log.error("[Fatal] Не удалось отправить неизвестное сообщение в Kafka: {}", e.getMessage());
            }

            throw new IllegalArgumentException("Route not found: " + routeName);
        }

        RouteConfig route = routeOpt.get();

        Message message = Message.builder()
                .id(UUID.randomUUID())
                .routeId(route.getId())
                .payload(jsonPayload)
                .status(MessageStatus.NEW)
                .createdAt(Instant.now())
                .headers(headers != null ? new HashMap<>(headers) : new HashMap<>())
                .context(new HashMap<>())
                .build();

        messageRepository.save(message);

        String inboundTopic = settingsService.getString("topic_inbound", "messages.new");
        log.info("[Ingestion] Сообщение {} сохранено в БД. Отправка в рабочий топик {}", message.getId(), inboundTopic);

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