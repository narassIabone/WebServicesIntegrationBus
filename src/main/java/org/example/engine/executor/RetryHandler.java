package org.example.engine.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.core.Message;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Слушатель для всех топиков повторных попыток (*-retry).
 * Реализует задержку перед возвратом сообщения в основной цикл обработки.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryHandler {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    /**
     * Слушает топики по паттерну. Например: route.HumanDataVerification.8.to.9-retry
     * * @param message Объект сообщения
     * @param retryTopic Название топика, из которого пришло сообщение (с суффиксом -retry)
     */
    @KafkaListener(topicPattern = ".*-retry", groupId = "esb-retry-group")
    public void handleRetry(Message message, @Header(KafkaHeaders.RECEIVED_TOPIC) String retryTopic) {
        log.info("[Retry-Handler] Получено сообщение {} для повтора из топика {}", message.getId(), retryTopic);

        try {
            TimeUnit.SECONDS.sleep(5);

            String originalTopic = retryTopic.replace("-retry", "");

            log.info("[Retry-Handler] Переотправка сообщения {} в оригинальный топик {}", message.getId(), originalTopic);

            kafkaTemplate.send(originalTopic, message);

        } catch (InterruptedException e) {
            log.error("[Retry-Handler] Ошибка во время паузы ретрая", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[Retry-Handler] Критическая ошибка при переотправке сообщения", e);
        }
    }
}