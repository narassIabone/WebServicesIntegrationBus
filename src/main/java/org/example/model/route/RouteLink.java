package org.example.model.route;

import java.util.Map;

public class RouteLink {
    private String fromNodeId;
    private String toNodeId;
    private String outputTopic;
    private boolean async; // true — Kafka, false — direct

    // Маппинг между полями сообщений
    // Ключ — имя поля во входящем сообщении, значение — имя поля в выходном
    private Map<String, String> fieldMapping;

    public RouteLink(String fromNodeId, String toNodeId, String outputTopic, boolean async, Map<String, String> fieldMapping) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.outputTopic = outputTopic;
        this.async = async;
        this.fieldMapping = fieldMapping;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }

    public String getoutputTopic() {
        return outputTopic;
    }

    public void setoutputTopic(String outputTopic) {
        this.outputTopic = outputTopic;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public Map<String, String> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(Map<String, String> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    @Override
    public String toString() {
        return "RouteLink{" +
                "fromNodeId='" + fromNodeId + '\'' +
                ", toNodeId='" + toNodeId + '\'' +
                ", kafkaTopicName='" + outputTopic + '\'' +
                ", async=" + async +
                ", fieldMapping=" + fieldMapping +
                '}';
    }
}
