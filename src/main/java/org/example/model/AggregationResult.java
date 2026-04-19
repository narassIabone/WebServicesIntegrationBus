package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AggregationResult {
    private final int receivedCount;
    private final int expectedCount;
    private final String rawPayloads;
    private final String rawContexts;

    public boolean isComplete() {
        return receivedCount >= expectedCount;
    }
}
