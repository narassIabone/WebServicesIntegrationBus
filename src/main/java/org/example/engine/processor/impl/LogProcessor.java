package org.example.engine.processor.impl;

import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.ProcessorResult;
import org.example.model.core.Message;
import org.example.model.route.NodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Процессор для логирования прохождения сообщения через узел.
 * Не изменяет payload, служит для отладки и мониторинга.
 * * Поддерживаемые параметры в NodeConfig:
 * - prefix: Строка, которая будет выводиться в начале лога (дефолт: "LOG").
 */
@Slf4j
@Component("LOG")
public class LogProcessor implements NodeProcessor {

    @Override
    public ProcessorResult process(Message message, NodeConfig config) {
        Map<String, String> nodeParams = config.getConfig();
        String customPrefix = (nodeParams != null) ? nodeParams.getOrDefault("prefix", "DEBUG") : "DEBUG";

        log.info("[Node {} (LOG)] === {} ===", config.getId(), customPrefix);
        log.info("[Node {} (LOG)] ID: {}", config.getId(), message.getId());
        log.info("[Node {} (LOG)] Payload: {}", config.getId(), message.getPayload());

        if (!message.getHeaders().isEmpty()) {
            log.info("[Node {} (LOG)] Headers: {}", config.getId(), message.getHeaders());
        }

        if (!message.getContext().isEmpty()) {
            log.info("[Node {} (LOG)] Context: {}", config.getId(), message.getContext());
        }

        log.info("[Node {} (LOG)] =======================", config.getId());

        return new ProcessorResult(List.of(
                new ProcessorResult.OutboundEnvelope(message, null)
        ));
    }
}