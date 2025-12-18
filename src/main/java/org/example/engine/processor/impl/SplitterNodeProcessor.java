package org.example.engine.processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.engine.processor.NodeProcessor;
import org.example.model.core.Message;
import org.example.model.core.MessageStatus;
import org.example.model.route.NodeConfig;
import org.example.service.KafkaProducerService;
import org.example.service.transformer.MessageMapper;

import java.util.Map;

// NodeType = SPLITTER
public class SplitterNodeProcessor implements NodeProcessor {

    private static final ObjectMapper mapper = new ObjectMapper();

    // Сервис для отправки сообщений в следующий узел
    private final KafkaProducerService kafkaProducerService;

    public SplitterNodeProcessor(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public Message process(Message parentMessage, NodeConfig config) throws Exception {

        // 1. Извлечение конфигурации узла
        Map<String, Object> nodeConfig = config.getConfig();
        String collectionPath = (String) nodeConfig.get("collectionPath");
        String nextTopic = (String) nodeConfig.get("nextTopic");
        Map<String, String> mappingRules = (Map<String, String>) nodeConfig.get("mappingRules");

        if (collectionPath == null || nextTopic == null) {
            throw new IllegalArgumentException("Splitter node requires 'collectionPath' and 'nextTopic'.");
        }

        String parentId = parentMessage.getId();
        JsonNode rootNode = mapper.readTree(parentMessage.getPayload());

        // Проверка и извлечение целевого массива
        JsonNode collectionNode = rootNode.get(collectionPath);

        if (collectionNode == null || !collectionNode.isArray()) {
            throw new RuntimeException("Splitter error: Path '" + collectionPath + "' is not a valid JSON array.");
        }

        // 2. Итерация и разделение
        for (JsonNode element : collectionNode) {

            // a) Создание дочернего сообщения
            String elementPayload = mapper.writeValueAsString(element);
            Message childMessage = new Message(elementPayload);

            // Копируем headers и контекст родителя
            childMessage.copyContextFrom(parentMessage);
            childMessage.setHeaders(parentMessage.getHeaders());

            // b) Установка parentId в контекст (для отслеживания)
            childMessage.getContext().put("parentId", parentId);

            // c) Маппинг (Трансформация) дочернего сообщения
            if (mappingRules != null && !mappingRules.isEmpty()) {
                String mappedPayload = MessageMapper.applyMapping(elementPayload, mappingRules);
                childMessage.setPayload(mappedPayload);
            }

            // d) Отправка в следующий Kafka-топик
            kafkaProducerService.route(childMessage, nextTopic);
        }

        // 3. Завершение обработки родительского сообщения
        parentMessage.setStatus(MessageStatus.SPLITTED);

        // Возвращаем null или сообщение со специальным статусом, чтобы оно не было обработано
        // как обычное сообщение на следующей стадии маршрутизации.
        return null;
    }
}