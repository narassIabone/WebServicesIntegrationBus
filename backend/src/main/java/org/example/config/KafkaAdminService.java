package org.example.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaAdminService {
    private final AdminClient adminClient;

    public KafkaAdminService(KafkaAdmin kafkaAdmin) {
        this.adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    @PreDestroy
    public void close() {
        adminClient.close();
    }

    public void ensureTopicExists(String topicName) {
        try {
            Set<String> topics = adminClient.listTopics().names().get();

            if (!topics.contains(topicName)) {
                log.info("[Admin] Топик {} не найден. Создаю...", topicName);

                NewTopic newTopic = new NewTopic(topicName, 12, (short) 1);
                adminClient.createTopics(Collections.singleton(newTopic)).all().get();

                log.info("[Admin] Топик {} успешно создан", topicName);
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            log.error("[Admin] Ошибка при проверке/создании топика: {}", topicName, e);
        }
    }
}
