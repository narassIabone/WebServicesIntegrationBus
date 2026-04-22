package org.example.engine.executor;

import lombok.extern.slf4j.Slf4j;
import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.NodeProcessorFactory;
import org.example.engine.processor.ProcessorResult;
import org.example.model.core.Message;
import org.example.model.core.MessageStatus;
import org.example.model.dto.AuditData;
import org.example.model.route.NodeConfig;
import org.example.model.route.RouteConfig;
import org.example.model.route.RouteLink;
import org.example.config.KafkaProducerService;
import org.example.service.AuditService;
import org.example.service.transformer.MessageMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RouteExecutor {

    @Value("${app.kafka.topics.error:messages.error}")
    private String errorTopic;

    @Value("${app.kafka.topics.final:messages.delivered}")
    private String finalTopic;

    @Value("${app.kafka.topics.inbound:messages.new}")
    private String inboundTopic;

    @Value("${app.executor.max-retries:3}")
    private int maxRetries;

    private final RouteCacheService routeCache;
    private final KafkaProducerService kafkaProducer;
    private final NodeProcessorFactory processorFactory;
    private final MessageMapper messageMapper;
    private final AuditService auditService;

    public RouteExecutor(RouteCacheService routeCache,
                         KafkaProducerService kafkaProducer,
                         NodeProcessorFactory processorFactory,
                         MessageMapper messageMapper,
                         AuditService auditService) {
        this.routeCache = routeCache;
        this.kafkaProducer = kafkaProducer;
        this.processorFactory = processorFactory;
        this.messageMapper = messageMapper;
        this.auditService = auditService;
    }

    @KafkaListener(
            topicPattern = "${app.kafka.topics.inbound:messages.new}|^(route\\.(?!.*-retry).*)$",
            groupId = "esb-runtime-group"
    )
    public void execute(@Payload Message message,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String currentTopic) {

        log.info("[Runtime] Получено сообщение {} из топика: {}", message.getId(), currentTopic);

        UUID traceId = initializeTraceId(message);
        long startTime = System.currentTimeMillis();

        // 1. Поиск конфигурации маршрута в кэше
        RouteConfig route = routeCache.getRoute(message.getRouteId());
        if (route == null) {
            log.error("[Error] Маршрут с ID {} не найден. Обработка сообщения {} прервана.",
                    message.getRouteId(), message.getId());
            return;
        }

        // 2. Определение необходимого узла по входящему топику
        NodeConfig currentNode = findNodeByInputTopic(route, currentTopic);
        if (currentNode == null) {
            log.error("[Error] Не найден узел для топика {} в маршруте {}. Сообщение: {}",
                    currentTopic, route.getId(), message.getId());
            return;
        }

        log.info("[Node {} ({})] Начало обработки сообщения {}. Полезная нагрузка: {}",
                currentNode.getId(), currentNode.getType(), message.getId(), message.getPayload());

        String payloadBefore = message.getPayload();

        // 3. Маппинг сообщения перед обработкой узлом
        applyMappingIfNecessary(message, route, currentNode);

        // 4. Выполнение узла и маршрутизация
        try {
            NodeProcessor processor = processorFactory.getProcessor(currentNode.getType());
            ProcessorResult result = processor.process(message, currentNode);

            long duration = System.currentTimeMillis() - startTime;
            sendAudit(traceId, message, currentNode, route.getId(), payloadBefore, duration, "SUCCESS", null);

            for (ProcessorResult.OutboundEnvelope envelope : result.getEnvelopes()) {
                Message msg = envelope.getMessage();
                Integer explicitTarget = envelope.getTargetNodeId();

                if (explicitTarget != null) {
                    String targetTopic = findTopicToSpecificNode(route, currentNode.getId(), explicitTarget);
                    log.info("[Node {} ({})] Прямая маршрутизация сообщения {} -> узел {} (топик: {})",
                            currentNode.getId(), currentNode.getType(), msg.getId(), explicitTarget, targetTopic);
                    kafkaProducer.route(msg, targetTopic);
                } else {
                    List<RouteLink> nextLinks = findAllNextLinks(route, currentNode.getId());

                    for (int i = 0; i < nextLinks.size(); i++) {
                        RouteLink link = nextLinks.get(i);
                        Message messageToSend = (i < nextLinks.size() - 1) ? msg.copy() : msg;
                        log.info("[Node {} ({})] Пересылка сообщения {} в следующий топик: {}",
                                currentNode.getId(), currentNode.getType(), messageToSend.getId(), link.getOutputTopic());
                        kafkaProducer.route(messageToSend, link.getOutputTopic());}

                    if (nextLinks.isEmpty()) {
                        finalizeRoute(msg);
                    }
                }
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            handleProcessingError(message, currentNode, currentTopic, e, traceId, payloadBefore, duration, route.getId());
        }
    }

    private void handleProcessingError(Message message, NodeConfig currentNode, String currentTopic,
                                       Exception e, UUID traceId, String payloadBefore, long duration, UUID routeId) {
        Throwable cause = (e.getCause() != null) ? e.getCause() : e;

        sendAudit(traceId, message, currentNode, routeId, payloadBefore, duration, "ERROR", cause.getMessage());
        if (cause instanceof org.example.engine.exception.RetryableException) {
            int currentRetry = Integer.parseInt(String.valueOf(message.getContext().getOrDefault("retry_count", "0")));

            if (currentRetry < maxRetries) {
                int nextRetry = currentRetry + 1;
                message.getContext().put("retry_count", String.valueOf(nextRetry));
                message.getContext().put("last_error", cause.getMessage());

                String retryTopic = currentTopic + "-retry";
                log.warn("[Retry] Попытка {}/{} для узла {}. Сообщение {} отправлено в {}",
                        nextRetry, maxRetries, currentNode.getId(), message.getId(), retryTopic);
                kafkaProducer.route(message, retryTopic);
            } else {
                sendToErrorQueue(message, "Превышено количество попыток: " + cause.getMessage(), currentNode);
            }
        } else {
            log.error("[Fatal] Ошибка в узле {}: {}, Сообщение: {}",
                    currentNode.getId(), cause.getMessage(), message.getId());
            sendToErrorQueue(message, cause.getMessage(), currentNode);
        }
    }

    private void applyMappingIfNecessary(Message message, RouteConfig route, NodeConfig currentNode) {
        RouteLink incomingLink = findIncomingLink(route, currentNode);
        if (incomingLink != null && incomingLink.getFieldMapping() != null && !incomingLink.getFieldMapping().isEmpty()) {
            String oldPayload = message.getPayload();
            String mappedPayload = prepareMessage(message, incomingLink);
            message.setPayload(mappedPayload);

            log.info("[Node {} ({})] Выполнен маппинг сообщения {}: данные изменены с [{}] на [{}]",
                    currentNode.getId(), currentNode.getType(), message.getId(), oldPayload, mappedPayload);
        } else {
            log.debug("[Node {} ({})] Маппинг пропущен: правила трансформации не заданы",
                    currentNode.getId(), currentNode.getType());
        }
    }

    private void sendAudit(UUID traceId, Message message, NodeConfig node, UUID routeId,
                           String payloadBefore, long duration, String status, String error) {
        auditService.logStep(AuditData.builder()
                .traceId(traceId)
                .messageId(message.getId())
                .routeId(routeId)
                .nodeId(node.getId())
                .nodeType(node.getType().name())
                .status(status)
                .payloadBefore(payloadBefore)
                .payloadAfter(message.getPayload())
                .headers(new HashMap<>(message.getHeaders()))
                .context(new HashMap<>(message.getContext()))
                .duration(duration)
                .errorMessage(error)
                .build());
    }

    private NodeConfig findNodeByInputTopic(RouteConfig route, String topic) {
        return route.getLinks().stream()
                .filter(link -> topic.equals(link.getOutputTopic()))
                .findFirst()
                .flatMap(link -> route.getNodes().stream()
                        .filter(node -> node.getId() == link.getToNodeId())
                        .findFirst())
                .orElseGet(() -> route.getNodes().stream()
                        .filter(node -> node.isStart() && topic.equals(inboundTopic))
                        .findFirst()
                        .orElse(null));
    }

    private String findTopicToSpecificNode(RouteConfig route, int fromId, int toId) {
        return route.getLinks().stream()
                .filter(l -> l.getFromNodeId() == fromId && l.getToNodeId() == toId)
                .map(RouteLink::getOutputTopic)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Связи между узлами" + fromId + " и " + toId + " не существует."));
    }

    private RouteLink findIncomingLink(RouteConfig route, NodeConfig currentNode) {
        return route.getLinks().stream()
                .filter(l -> l.getToNodeId() == currentNode.getId())
                .findFirst()
                .orElse(null);
    }

    private List<RouteLink> findAllNextLinks(RouteConfig route, int currentNodeId) {
        return route.getLinks().stream()
                .filter(link -> link.getFromNodeId() == currentNodeId)
                .collect(Collectors.toList());
    }

    private String prepareMessage(Message message, RouteLink link) {
        try {
            return messageMapper.applyMapping(message.getPayload(), link.getFieldMapping());
        } catch (Exception e) {
            log.error("[Mapping] Ошибка трансформации сообщения {}: {}", message.getId(), e.getMessage());
            throw new RuntimeException("Mapping failed: " + e.getMessage(), e);
        }
    }

    private void finalizeRoute(Message msg) {
        msg.setStatus(MessageStatus.DELIVERED);
        log.info("[Completed] Сообщение {} успешно доставлено в финальный топик {}",
                msg.getId(), finalTopic);
        kafkaProducer.route(msg, finalTopic);
    }

    private void sendToErrorQueue(Message message, String reason, NodeConfig currentNode) {
        log.error("[Fatal] Сообщение {} перемещено в очередь ошибок {}. Причина: {}",
                message.getId(), errorTopic, reason);

        message.getContext().put("error_reason", reason);

        if (currentNode != null) {
            message.getContext().put("error_node_id", String.valueOf(currentNode.getId()));
        }

        kafkaProducer.route(message, errorTopic);
    }

    private UUID initializeTraceId(Message message) {
        Object existingTraceId = message.getContext().get("trace_id");

        if (existingTraceId == null) {
            String newTraceId = UUID.randomUUID().toString();
            message.getContext().put("trace_id", newTraceId);
            return UUID.fromString(newTraceId);
        }

        return UUID.fromString(existingTraceId.toString());
    }
}