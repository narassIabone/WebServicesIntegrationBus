package org.example.engine.processor;

import org.example.model.enums.NodeType;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class NodeProcessorFactory {

    private final Map<String, NodeProcessor> processors;

    public NodeProcessorFactory(Map<String, NodeProcessor> processors) {
        this.processors = processors;
    }

    public NodeProcessor getProcessor(NodeType type) {
        NodeProcessor processor = processors.get(type.name());
        if (processor == null) {
            throw new IllegalArgumentException("Процессор не найден для типа: " + type);
        }
        return processor;
    }
}