package org.example.engine.processor.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.engine.exception.FatalException;
import org.example.engine.exception.RetryableException;
import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.ProcessorResult;
import org.example.model.entity.AggregationResult;
import org.example.model.core.Message;
import org.example.model.route.NodeConfig;
import org.example.service.AggregationService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;


/**
 * Процессор-агрегатор для объединения нескольких сообщений (фрагментов) в одно целое.
 * Использует внешнее хранилище (PostgreSQL) для накопления данных до достижения заданного порога.
 * --- ПАРАМЕТРЫ (NodeConfig params) ---
 * - expected_count: Количество фрагментов, которое необходимо собрать для завершения группы (дефолт: 2).
 */
@Slf4j
@Component("AGGREGATOR")
public class AggregatorProcessor implements NodeProcessor {

    private final AggregationService aggregationService;
    private final ObjectMapper objectMapper;

    public AggregatorProcessor(AggregationService aggregationService, ObjectMapper objectMapper) {
        this.aggregationService = aggregationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProcessorResult process(Message message, NodeConfig config) {
        Object correlationObj = message.getContext().get("correlationId");
        if (correlationObj == null) {
            log.error("[Error] Агрегатор: отсутствует correlationId в сообщении {}", message.getId());
            throw new FatalException("Aggregator: отсутствует correlationId в контексте сообщения " + message.getId());
        }

        UUID correlationId = UUID.fromString(correlationObj.toString());
        int expectedCount = Integer.parseInt(config.getConfig().getOrDefault("expected_count", "2"));

        log.info("[Node {} (AGGREGATOR)] Обработка фрагмента для группы {}. Ожидаем: {}",
                config.getId(), correlationId, expectedCount);

        AggregationResult result;
        try {
            result = aggregationService.addFragment(
                    correlationId,
                    message.getPayload(),
                    message.getContext(),
                    expectedCount
            );
        } catch (Exception e) {
            log.warn("[Retry] Ошибка сохранения фрагмента в БД для группы {}: {}", correlationId, e.getMessage());
            throw new RetryableException("Aggregator: ошибка при сохранении фрагмента в БД: " + e.getMessage());
        }

        log.info("[Node {} (AGGREGATOR)] Группа {}: получено {} из {}",
                config.getId(), correlationId, result.getReceivedCount(), result.getExpectedCount());

        if (result.getReceivedCount() >= result.getExpectedCount()) {
            log.info("[Node {} (AGGREGATOR)] Группа {} ПОЛНОСТЬЮ СОБРАНА. Начинаем слияние данных.",
                    config.getId(), correlationId);

            message.setPayload(result.getRawPayloads());

            try {
                Map<String, Object> mergedContext = objectMapper.readValue(
                        result.getRawContexts(),
                        new TypeReference<Map<String, Object>>() {}
                );
                message.setContext(mergedContext);
            } catch (Exception e) {
                log.error("[Error] Не удалось десериализовать накопленный контекст для группы {}", correlationId);
                throw new FatalException("Aggregator: не удалось распарсить накопленный контекст: " + e.getMessage());
            }

            try {
                aggregationService.cleanUp(correlationId);
                log.debug("[Node {} (AGGREGATOR)] Данные группы {} удалены из временного хранилища",
                        config.getId(), correlationId);
            } catch (Exception e) {
                log.warn("[Error] Ошибка при очистке группы {}: {}", correlationId, e.getMessage());
            }

            return ProcessorResult.builder()
                    .envelope(new ProcessorResult.OutboundEnvelope(message, null))
                    .build();
        }

        log.info("[Node {} (AGGREGATOR)] Сообщение {} поглощено. Ожидаем остальные фрагменты группы {}.",
                config.getId(), message.getId(), correlationId);

        return ProcessorResult.builder().build();
    }
}
