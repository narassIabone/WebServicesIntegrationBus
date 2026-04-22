package org.example.engine.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.engine.exception.FatalException;
import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.ProcessorResult;
import org.example.model.core.Message;
import org.example.model.route.NodeConfig;
import org.springframework.stereotype.Component;

/**
 * Процессор для разделения (сплиттинга) входящего сообщения на несколько независимых сообщений.
 * Добавляет поле correlationId в context для связи между разделенными сообщениями
 */
@Slf4j
@Component("SPLITTER")
public class SplitterProcessor implements NodeProcessor {

    @Override
    public ProcessorResult process(Message message, NodeConfig config) {
        if (!message.getContext().containsKey("correlationId")) {
            String correlationId = message.getId().toString();
            message.getContext().put("correlationId", correlationId);
            log.debug("[Node {} (SPLITTER)] Установлен Correlation ID: {}", config.getId(), correlationId);
        }

        try {
            log.info("[Node {} (SPLITTER)] Подготовка сообщения {} к разделению/пробросу",
                    config.getId(), message.getId());

            return ProcessorResult.builder()
                    .envelope(ProcessorResult.OutboundEnvelope.builder()
                            .message(message)
                            .targetNodeId(null)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("[Error] Сбой в работе Splitter (Node {}): {}", config.getId(), e.getMessage());
            throw new FatalException("Splitter Node " + config.getId() + " failed: " + e.getMessage());
        }
    }
}