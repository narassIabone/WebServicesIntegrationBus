package org.example.engine.processor;

import org.example.model.core.Message;
import org.example.model.route.NodeConfig;

public interface NodeProcessor {
    ProcessorResult process(Message message, NodeConfig config) throws Exception;
}
