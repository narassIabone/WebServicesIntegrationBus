package org.example.engine.executor;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.example.model.route.NodeConfig;
import org.example.model.route.RouteConfig;
import org.example.model.route.RouteLink;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class RouteAdminService {

    private final KafkaAdmin kafkaAdmin;

    @Value("${app.kafka.topics.inbound:messages.new}")
    private String inboundTopic;

    public RouteAdminService(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    public void prepareRouteConfig(RouteConfig route) {
        String routeName = route.getName() != null ? route.getName() : "default";
        Set<String> topicsToCreate = new HashSet<>();

        // 1. Помечаем стартовый узел
        for (NodeConfig node : route.getNodes()) {
            if (node.isStart()) {
                node.setInputTopic(inboundTopic);
            }
        }

        // 2. Генерируем топики и собираем их в список
        for (RouteLink link : route.getLinks()) {
            String generatedTopic = String.format("route.%s.%s.to.%s",
                    routeName,
                    link.getFromNodeId(),
                    link.getToNodeId());

            link.setOutputTopic(generatedTopic);
            topicsToCreate.add(generatedTopic); // Добавляем в очередь на создание

            route.getNodes().stream()
                    .filter(n -> n.getId() == link.getToNodeId())
                    .findFirst()
                    .ifPresent(targetNode -> targetNode.setInputTopic(generatedTopic));
        }

        // 3. Создаем топики в Kafka
        createTopicsInKafka(topicsToCreate);
    }

    private void createTopicsInKafka(Set<String> topics) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            List<NewTopic> newTopics = topics.stream()
                    .map(topic -> new NewTopic(topic, 1, (short) 1)) // 1 партиция, 1 реплика
                    .collect(Collectors.toList());

            adminClient.createTopics(newTopics).all().get();
            // .get() заставит подождать, пока Kafka подтвердит создание
        } catch (Exception e) {
            // В НИР/ВКР стоит упомянуть, что здесь можно ловить TopicExistsException
            System.err.println("Ошибка при создании топиков: " + e.getMessage());
        }
    }
}