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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryHandler {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    @KafkaListener(topicPattern = ".*-retry", groupId = "esb-retry-group")
    public void handleRetry(Message message, @Header(KafkaHeaders.RECEIVED_TOPIC) String retryTopic) {
        String retryCount = (String) message.getContext().getOrDefault("retry_count", "1");

        log.info("[Retry] Получено сообщение {} для повтора (попытка №{}). Топик: {}",
                message.getId(), retryCount, retryTopic);

        try {
            TimeUnit.SECONDS.sleep(5);

            String originalTopic = retryTopic.replace("-retry", "");

            log.info("[Retry] Возврат сообщения {} в основной поток обработки (топик: {})",
                    message.getId(), originalTopic);

            kafkaTemplate.send(originalTopic, message);

        } catch (InterruptedException e) {
            log.error("[Error] Прерывание ожидания ретрая для сообщения {}", message.getId(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[Error] Не удалось вернуть сообщение {} в оригинальный топик", message.getId(), e);
        }
    }
}