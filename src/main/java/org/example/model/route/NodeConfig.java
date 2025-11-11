package org.example.model.route;

import java.util.List;
import java.util.Map;

public class NodeConfig {
    private String id;
    private NodeType type;
    private String kafkaTopic;
    private Map<String, Object> config;
    private List<String> nextNodes;

    public NodeConfig() {}

    public NodeConfig(String id, NodeType type, String kafkaTopic,
                      Map<String, Object> config, List<String> nextNodes) {
        this.id = id;
        this.type = type;
        this.kafkaTopic = kafkaTopic;
        this.config = config;
        this.nextNodes = nextNodes;
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

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public List<String> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<String> nextNodes) {
        this.nextNodes = nextNodes;
    }

    @Override
    public String toString() {
        return "NodeConfig{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", kafkaTopic='" + kafkaTopic + '\'' +
                ", config=" + config +
                ", nextNodes=" + nextNodes +
                '}';
    }
}
