package org.example.engine.executor;

import java.util.List;
import java.util.Map;

public class NodeRuntimeContext {

    private final String nodeId;
    private final List<String> inputTopics;
    private final List<String> outputTopics;
    private final Map<String, Object> parameters;

    public NodeRuntimeContext(
            String nodeId,
            List<String> inputTopics,
            List<String> outputTopics,
            Map<String, Object> parameters
    ) {
        this.nodeId = nodeId;
        this.inputTopics = inputTopics;
        this.outputTopics = outputTopics;
        this.parameters = parameters;
    }

    public String getNodeId() {
        return nodeId;
    }

    public List<String> getInputTopics() {
        return inputTopics;
    }

    public List<String> getOutputTopics() {
        return outputTopics;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
