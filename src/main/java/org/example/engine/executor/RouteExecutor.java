package org.example.engine.executor;

import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.NodeProcessorFactory;
import org.example.engine.processor.ProcessorResult;
import org.example.model.core.Message;
import org.example.model.core.MessageStatus;
import org.example.model.route.NodeConfig;
import org.example.model.route.RouteConfig;
import org.example.model.route.RouteLink;
import org.example.config.KafkaProducerService;
import org.example.service.transformer.MessageMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

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

    public RouteExecutor(RouteCacheService routeCache,
                         KafkaProducerService kafkaProducer,
                         NodeProcessorFactory processorFactory,
                         MessageMapper messageMapper) {
        this.routeCache = routeCache;
        this.kafkaProducer = kafkaProducer;
        this.processorFactory = processorFactory;
        this.messageMapper = messageMapper;
    }

    @KafkaListener(
            topicPattern = "${app.kafka.topics.inbound:messages.new}|^(route\\.(?!.*-retry).*)$",
            groupId = "esb-runtime-group"
    )
    public void execute(@Payload Message message,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String currentTopic) {

        System.out.println("[Runtime] Processing message " + message.getId() + " on topic: " + currentTopic);

        // 1. Поиск конфигурации маршрута в кэше
        RouteConfig route = routeCache.getRoute(message.getRouteId());
        if (route == null) {
            System.err.println("[Error] Route not found for ID: " + message.getRouteId());
            return;
        }

        // 2. Определение необходимого узла по входящему топику
        NodeConfig currentNode = findNodeByInputTopic(route, currentTopic);
        if (currentNode == null) {
            System.err.println("[Error] No node configured for topic: " + currentTopic);
            return;
        }

        System.out.println("[Runtime: Node " + currentNode.getId() + "] received message" + message.getId() +
                " with payload " + message.getPayload() + " from topic: " + currentTopic);

        // 3. Маппинг сообщения перед обработкой узлом
        RouteLink incomingLink = findIncomingLink(route, currentNode);

        if (incomingLink != null) {
            String oldPayload = message.getPayload();
            String mappedPayload = prepareMessage(message, incomingLink);
            message.setPayload(mappedPayload);

            System.out.println("[Runtime: Node " + currentNode.getId() + "] mapped payload of the message" + message.getId() +
                    " from " + oldPayload + " to " + mappedPayload);
        }

        // 4. Выполнение узла и маршрутизация
        try {
            NodeProcessor processor = processorFactory.getProcessor(currentNode.getType());
            ProcessorResult result = processor.process(message, currentNode);

            for (ProcessorResult.OutboundEnvelope envelope : result.getEnvelopes()) {
                Message msg = envelope.getMessage();
                Integer explicitTarget = envelope.getTargetNodeId();

                if (explicitTarget != null) {
                    String targetTopic = findTopicToSpecificNode(route, currentNode.getId(), explicitTarget);
                    System.out.println("[Runtime: Node " + currentNode.getId() + "] Sensing processed message " + message.getId()
                            + " with payload " + message.getPayload() +" to topic: " + targetTopic);
                    kafkaProducer.route(msg, targetTopic);
                } else {
                    List<RouteLink> nextLinks = findAllNextLinks(route, currentNode.getId());

                    for (int i = 0; i < nextLinks.size(); i++) {
                        RouteLink link = nextLinks.get(i);
                        Message messageToSend = (i < nextLinks.size() - 1) ? msg.copy() : msg;
                        kafkaProducer.route(messageToSend, link.getOutputTopic());
                        System.out.println("[Runtime: Node " + currentNode.getId() + "] Sensing processed message " + message.getId()
                                + " with payload " + message.getPayload() +" to topic: " + link.getOutputTopic());
                    }

                    if (nextLinks.isEmpty()) {
                        finalizeRoute(msg);
                    }
                }
            }

        } catch (Exception e) {
            Throwable cause = (e.getCause() != null) ? e.getCause() : e;

            if (cause instanceof org.example.engine.exception.RetryableException) {
                int currentRetry = Integer.parseInt(String.valueOf(message.getContext().getOrDefault("retry_count", "0")));

                if (currentRetry < maxRetries) {
                    message.getContext().put("retry_count", String.valueOf(currentRetry + 1));
                    message.getContext().put("last_error", cause.getMessage());

                    String retryTopic = currentTopic + "-retry";
                    System.out.println("[Retry] Попытка " + (currentRetry + 1) + " для ноды " + currentNode.getId() + ". Отправка в " + retryTopic);
                    kafkaProducer.route(message, retryTopic);
                } else {
                    sendToErrorQueue(message, "Max retries reached: " + cause.getMessage());
                }
            }
            else {
                sendToErrorQueue(message, cause.getMessage());
            }
        }
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
            throw new RuntimeException("Mapping failed: " + e.getMessage(), e);
        }
    }

    private void finalizeRoute(Message msg) {
        msg.setStatus(MessageStatus.DELIVERED);
        kafkaProducer.route(msg, finalTopic);
        System.out.println("[Completed] Message " + msg.getId() + " reached messages.delivered");
    }

    private void sendToErrorQueue(Message message, String reason) {
        System.err.println("[Fatal] Сообщение " + message.getId() + " отправлено в messages.error. Причина: " + reason);
        message.getContext().put("error_reason", reason);
        message.getContext().put("error_node_id", String.valueOf(message.getId()));
        kafkaProducer.route(message, errorTopic);
    }
}