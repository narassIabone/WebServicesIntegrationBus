package org.example.engine.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.example.model.core.Message;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProcessorResult {

    @Singular
    private List<OutboundEnvelope> envelopes;

    @Data
    @AllArgsConstructor
    @Builder
    public static class OutboundEnvelope {
        private Message message;
        private Integer targetNodeId;
    }
}