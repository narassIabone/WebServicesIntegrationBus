package org.example.engine;

import org.example.engine.node_processors.RestCallNodeProcessor;
import org.example.model.route.NodeType;

import java.util.Map;

public class NodeProcessorFactory {

    private static final Map<NodeType, NodeProcessor> processors = Map.of(
            NodeType.REST_CALL, new RestCallNodeProcessor()
            // Добавим позже: KAFKA_PUBLISH, DB_QUERY, VALIDATION и т.д.
    );

    public static NodeProcessor get(NodeType type) {
        return processors.get(type);
    }
}
