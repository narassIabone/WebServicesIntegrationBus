package org.example.engine.processor;

import org.example.model.entity.Message;
import org.example.model.entity.NodeConfig;

public interface NodeProcessor {
    ProcessorResult process(Message message, NodeConfig config) throws Exception;
}
