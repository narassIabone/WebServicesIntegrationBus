package org.example.engine.processor.impl;

import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.ProcessorResult;
import org.example.model.core.Message;
import org.example.model.route.NodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Процессор для логирования прохождения сообщения через узел.
 * Не изменяет payload, служит для отладки и мониторинга.
 * * Поддерживаемые параметры в NodeConfig:
 * - prefix: Строка, которая будет выводиться в начале лога (дефолт: "LOG").
 */
@Component("LOG")
public class LogProcessor implements NodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(LogProcessor.class);

    @Override
    public ProcessorResult process(Message message, NodeConfig config) {
        Map<String, String> nodeParams = config.getConfig();

        String prefix = "LOG";
        if (nodeParams != null && nodeParams.containsKey("prefix")) {
            prefix = nodeParams.get("prefix");
        }

        log.info("[{}] Received message: id={}, status={}", prefix, message.getId(), message.getStatus());
        log.info("[{}] Payload: {}", prefix, message.getPayload());

        return new ProcessorResult(List.of(
                new ProcessorResult.OutboundEnvelope(message, null)
        ));
    }
}