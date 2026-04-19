package org.example.engine.processor.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.engine.exception.FatalException;
import org.example.engine.exception.RetryableException;
import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.ProcessorResult;
import org.example.model.AggregationResult;
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
            throw new FatalException("Aggregator: отсутствует correlationId в контексте сообщения " + message.getId());
        }

        UUID correlationId = UUID.fromString(correlationObj.toString());
        int expectedCount = Integer.parseInt(config.getConfig().getOrDefault("expected_count", "2"));

        AggregationResult result;
        try {
            result = aggregationService.addFragment(
                    correlationId,
                    message.getPayload(),
                    message.getContext(),
                    expectedCount
            );
        } catch (Exception e) {
            throw new RetryableException("Aggregator: ошибка при сохранении фрагмента в БД: " + e.getMessage());
        }

        if (result.getReceivedCount() >= result.getExpectedCount()) {
            System.out.println("[Aggregator] Group " + correlationId + " is COMPLETE");

            message.setPayload(result.getRawPayloads());

            try {
                Map<String, Object> mergedContext = objectMapper.readValue(
                        result.getRawContexts(),
                        new TypeReference<Map<String, Object>>() {}
                );
                message.setContext(mergedContext);
            } catch (Exception e) {
                throw new FatalException("Aggregator: не удалось распарсить накопленный контекст: " + e.getMessage());
            }

            try {
                aggregationService.cleanUp(correlationId);
            } catch (Exception e) {
                System.err.println("[Aggregator] Warning: CleanUp failed: " + e.getMessage());
            }

            return ProcessorResult.builder()
                    .envelope(new ProcessorResult.OutboundEnvelope(message, null))
                    .build();
        }

        return ProcessorResult.builder().build();
    }
}
