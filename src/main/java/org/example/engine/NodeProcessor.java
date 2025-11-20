package org.example.engine;

import org.example.model.Message;
import org.example.model.route.NodeConfig;

import java.util.List;

public interface NodeProcessor {
    Message process(Message input, NodeConfig nodeConfig) throws Exception;
}
