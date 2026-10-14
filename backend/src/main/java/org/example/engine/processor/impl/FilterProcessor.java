package org.example.engine.processor.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.KafkaProducerService;
import org.example.engine.exception.FatalException;
import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.ProcessorResult;
import org.example.model.entity.Message;
import org.example.model.entity.NodeConfig;
import org.example.service.SystemSettingsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Процессор для логической фильтрации и управления потоком сообщений.
 * Позволяет выполнять ветвление или остановку маршрута на основе условий (SpEL).
 * * --- КОНФИГУРАЦИЯ (NodeConfig params) ---
 * 1. expression: String (обязательно)
 * SpEL-выражение. Доступные переменные: #payload, #headers, #context.
 * Пример: "#context['2.http_status'] == '200' and #payload['amount'] > 100"
 * * 2. ifFalse: String (дефолт: "CONTINUE")
 * Определяет поведение, если условие НЕ выполнено:
 * - "CONTINUE": (Вариант 1) Идти дальше по стандартным связям (links).
 * - "DIVERGE":  (Вариант 2) Перейти в конкретную ноду, указанную в 'on_false_target_node'.
 * - "FINISH":   (Вариант 3) Завершить маршрут. Сообщение отправляется в топик 'messages.delivered'.
 * * 3. on_false_target_node: String (ID ноды)
 * Используется только если ifFalse = "DIVERGE".
 * * --- ТРАССИРОВКА (Context) ---
 * В контекст сообщения всегда добавляются поля:
 * - [nodeId].filter.result: "MATCHED" или "SKIPPED"
 * - [nodeId].filter.expression: само проверяемое выражение
 */

@Slf4j
@Component("FILTER")
public class FilterProcessor implements NodeProcessor {

    private final ObjectMapper objectMapper;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final KafkaProducerService kafkaProducerService;
    private final SystemSettingsService settingsService;

    public FilterProcessor(ObjectMapper objectMapper,
                           KafkaProducerService kafkaProducerService,
                           SystemSettingsService settingsService) {
        this.objectMapper = objectMapper;
        this.kafkaProducerService = kafkaProducerService;
        this.settingsService = settingsService;
    }

    @Override
    public ProcessorResult process(Message message, NodeConfig config) {
        Map<String, String> params = config.getConfig();
        String expressionStr = params.get("expression");

        if (expressionStr == null || expressionStr.isBlank()) {
            log.error("[Error] Фильтр (Node {}): не задано выражение для проверки", config.getId());
            throw new FatalException("Filter Node " + config.getId() + ": выражение (expression) не задано");
        }

        String ifFalseAction = params.getOrDefault("ifFalse", "CONTINUE");
        String falseTargetNodeId = params.get("on_false_target_node");

        log.info("[Node 'FILTER' ({})] Проверка условия: [{}]", config.getName(), expressionStr);

        boolean isMatched;
        try {
            isMatched = evaluate(expressionStr, message, config.getName());
        } catch (Exception e) {
            log.error("[Error] Ошибка вычисления SpEL в ноде {}: {}", config.getId(), e.getMessage());
            throw new FatalException("Ошибка вычисления фильтра [" + expressionStr + "]: " + e.getMessage());
        }

        String nodePrefix = config.getId() + ".filter";
        message.getContext().put(nodePrefix + ".result", isMatched ? "MATCHED" : "SKIPPED");
        message.getContext().put(nodePrefix + ".expression", expressionStr);

        if (isMatched) {
            log.info("[Node 'FILTER' ({})] Условие выполнено (MATCHED). Сообщение продолжает маршрут.", config.getName());
            return ProcessorResult.builder()
                    .envelope(new ProcessorResult.OutboundEnvelope(message, null))
                    .build();
        }

        log.info("[Node 'FILTER' ({})] Условие НЕ выполнено (SKIPPED). Применяется действие: {}", config.getName(), ifFalseAction);

        switch (ifFalseAction.toUpperCase()) {
            case "CONTINUE":
                return ProcessorResult.builder()
                        .envelope(new ProcessorResult.OutboundEnvelope(message, null))
                        .build();

            case "DIVERGE":
                if (falseTargetNodeId == null || falseTargetNodeId.isBlank()) {
                    throw new FatalException("Filter Node " + config.getId() + ": отсутствует on_false_target_node для DIVERGE");
                }
                log.info("[Node 'FILTER' ({})] Дивергенция: перенаправление сообщения в узел {}", config.getName(), falseTargetNodeId);
                return ProcessorResult.builder()
                        .envelope(new ProcessorResult.OutboundEnvelope(message, Integer.parseInt(falseTargetNodeId)))
                        .build();

            case "FINISH":
                log.info("[Node 'FILTER' ({})] Завершение маршрута согласно настройкам фильтра.", config.getName());
                message.getContext().put(nodePrefix + ".exit_reason", "FILTER_FINISH");

                String finalTopic = settingsService.getString("topic_final", "messages.delivered");

                log.info("[Filter] Отправка отфильтрованного сообщения {} в финальный топик: {}",
                        message.getId(), finalTopic);

                kafkaProducerService.route(message, finalTopic);
                return ProcessorResult.builder().build();

            default:
                throw new FatalException("Filter Node " + config.getId() + ": неизвестный action: " + ifFalseAction);
        }
    }

    private boolean evaluate(String expressionStr, Message message, String nodeName) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("headers", message.getHeaders());
        context.setVariable("context", message.getContext());

        try {
            Map<String, Object> payloadMap = objectMapper.readValue(
                    message.getPayload(),
                    new TypeReference<Map<String, Object>>() {}
            );
            context.setVariable("payload", payloadMap);
        } catch (Exception e) {
            log.debug("[Node 'FILTER' ({})] Payload не является JSON, используется как String", nodeName);
            context.setVariable("payload", message.getPayload());
        }

        Expression exp = parser.parseExpression(expressionStr);
        return Boolean.TRUE.equals(exp.getValue(context, Boolean.class));
    }
}
