package org.example.engine.processor.impl;

import org.example.engine.exception.FatalException;
import org.example.engine.processor.NodeProcessor;
import org.example.engine.processor.ProcessorResult;
import org.example.model.core.Message;
import org.example.model.route.NodeConfig;
import org.springframework.stereotype.Component;

@Component("SPLITTER")
public class SplitterProcessor implements NodeProcessor {

    @Override
    public ProcessorResult process(Message message, NodeConfig config) {
        if (!message.getContext().containsKey("correlationId")) {
            message.getContext().put("correlationId", message.getId().toString());
        }

        try {
            return ProcessorResult.builder()
                    .envelope(ProcessorResult.OutboundEnvelope.builder()
                            .message(message)
                            .targetNodeId(null)
                            .build())
                    .build();

        } catch (Exception e) {
            throw new FatalException("Splitter Node " + config.getId() + " failed: " + e.getMessage());
        }
    }
}