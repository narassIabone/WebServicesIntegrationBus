package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.AggregationResult;
import org.example.repository.AggregationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final AggregationRepository repository;

    @Transactional
    public AggregationResult addFragment(UUID correlationId, String payload, Map<String, Object> context, int expectedCount) {
        return repository.upsertFragment(correlationId, payload, context, expectedCount);
    }

    @Transactional
    public void cleanUp(UUID correlationId) {
        repository.deleteByCorrelationId(correlationId);
    }
}
