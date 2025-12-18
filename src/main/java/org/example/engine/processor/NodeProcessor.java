package org.example.engine.processor;

import org.example.engine.executor.NodeRuntimeContext;

public interface NodeProcessor {
    void init(NodeRuntimeContext context);

    void start();

    void stop();
}
