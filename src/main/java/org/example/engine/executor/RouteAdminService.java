package org.example.engine.executor;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class RouteAdminService {

    private final KafkaAdmin kafkaAdmin;

    @Value("${app.kafka.topics.inbound:messages.new}")
    private String inboundTopic;

    public RouteAdminService(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    public void prepareRouteConfig(RouteConfig route) {
        log.info("[Admin] Подготовка инфраструктуры для маршрута: {} (ID: {})", route.getName(), route.getId());

        String routeName = route.getName() != null ? route.getName() : "default";
        Set<String> topicsToCreate = new HashSet<>();

        for (NodeConfig node : route.getNodes()) {
            if (node.isStart()) {
                node.setInputTopic(inboundTopic);
                log.debug("[Admin] Узел {} помечен как стартовый. Входной топик: {}", node.getId(), inboundTopic);
            }
        }

        for (RouteLink link : route.getLinks()) {
            String generatedTopic = String.format("route.%s.%s.to.%s",
                    routeName,
                    link.getFromNodeId(),
                    link.getToNodeId());

            link.setOutputTopic(generatedTopic);
            topicsToCreate.add(generatedTopic);
            topicsToCreate.add(generatedTopic + "-retry");

            route.getNodes().stream()
                    .filter(n -> n.getId() == link.getToNodeId())
                    .findFirst()
                    .ifPresent(targetNode -> targetNode.setInputTopic(generatedTopic));
        }

        log.info("[Admin] Сформирован список из {} топиков для создания", topicsToCreate.size());
        createTopicsInKafka(topicsToCreate);
    }

    private void createTopicsInKafka(Set<String> topics) {
        if (topics.isEmpty()) {
            log.warn("[Admin] Список топиков пуст, создание не требуется");
            return;
        }

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            List<NewTopic> newTopics = topics.stream()
                    .map(topic -> new NewTopic(topic, 1, (short) 1))
                    .collect(Collectors.toList());

            adminClient.createTopics(newTopics).all().get();
            log.info("[Admin] Топики успешно созданы/проверены в Kafka: {}", topics);

        } catch (Exception e) {
            log.error("[Error] Критический сбой при создании топиков в Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Infrastructure preparation failed", e);
        }
    }
}