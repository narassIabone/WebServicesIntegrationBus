package org.example.service;

import org.example.config.KafkaProducerService;
import org.example.model.core.Message;
import org.example.model.route.RouteConfig;
import org.example.repository.MessageRepository;
import org.example.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;

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
        Object payloadObj = request.get("payload");
        String routeName = (String) request.get("routeName");

        if (payloadObj == null || routeName == null) {
            throw new IllegalArgumentException("Payload and routeName are required");
        }

        RouteConfig route = routeRepository.findByName(routeName)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeName));

        Message message = Message.builder()
                .id(UUID.randomUUID())
                .routeId(route.getId())
                .payload(payloadObj.toString())
                .status(org.example.model.core.MessageStatus.NEW)
                .createdAt(java.time.Instant.now())
                .headers(new java.util.HashMap<>())
                .context(new java.util.HashMap<>())
                .build();

        Map<String, Object> headers = (Map<String, Object>) request.get("headers");
        if (headers != null) {
            message.getHeaders().putAll(headers);
        }

        messageRepository.save(message);

        kafkaProducerService.route(message, inboundTopic);

        return message.getId().toString();
    }
}