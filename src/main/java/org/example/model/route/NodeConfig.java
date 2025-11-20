package org.example.model.route;

import java.util.Map;

public class NodeConfig {
    private String id;
    private NodeType type;
    private String inputTopic;
    private Map<String, Object> config;

    public NodeConfig() {}

    public NodeConfig(String id, NodeType type, String inputTopic, Map<String, Object> config) {
        this.id = id;
        this.type = type;
        this.inputTopic = inputTopic;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    @Override
    public String toString() {
        return "NodeConfig{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", config=" + config +
                '}';
    }
}
