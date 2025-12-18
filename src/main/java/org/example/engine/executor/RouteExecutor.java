package org.example.engine.executor;

import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.NodeProcessorFactory;
import org.example.model.route.NodeConfig;
import org.example.model.route.RouteConfig;
import org.example.model.route.RouteLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteExecutor {

    private final KafkaAdmin kafkaAdmin;
    private final NodeProcessorFactory nodeProcessorFactory;

    public RouteExecutor(
            KafkaAdmin kafkaAdmin,
            NodeProcessorFactory nodeProcessorFactory
    ) {
        this.kafkaAdmin = kafkaAdmin;
        this.nodeProcessorFactory = nodeProcessorFactory;
    }

    public void startRoute(RouteConfig routeConfig) {
        //ключ → nodeId
        //значение → список Kafka-топиков, которые узел должен слушать
        Map<String, List<String>> inputTopics = new HashMap<>();

        //ключ → nodeId
        //значение → список Kafka-топиков, в которые узел должен публиковать
        Map<String, List<String>> outputTopics = new HashMap<>();


        // 1. Создаём топики по RouteLink
        for (RouteLink link : routeConfig.getLinks()) {
            String topicName = buildTopicName(routeConfig.getId(), link);
            createTopic(topicName);

            outputTopics
                    .computeIfAbsent(link.getFromNodeId(), k -> new ArrayList<>())
                    .add(topicName);

            inputTopics
                    .computeIfAbsent(link.getToNodeId(), k -> new ArrayList<>())
                    .add(topicName);
        }

        // 2. Инициализируем узлы
        for (NodeConfig nodeConfig : routeConfig.getNodes()) {

            NodeRuntimeContext context = new NodeRuntimeContext(
                    nodeConfig.getId(),
                    inputTopics.getOrDefault(nodeConfig.getId(), List.of()),
                    outputTopics.getOrDefault(nodeConfig.getId(), List.of()),
                    nodeConfig.getConfig()
            );

            NodeProcessor processor = nodeProcessorFactory.create(nodeConfig);
            processor.init(context);
            processor.start();
        }
    }

    private void createTopic(String topicName) {
        NewTopic topic = new NewTopic(topicName, 1, (short) 1);
        kafkaAdmin.createOrModifyTopics(topic);
    }

    private String buildTopicName(String routeId, RouteLink link) {
        return routeId + "." + link.getFromNodeId() + "." + link.getToNodeId();
    }
}

