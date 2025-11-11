package org.example.model.route;

public class RouteLink {
    private String fromNodeId;
    private String toNodeId;
    private String kafkaTopicName; // Kafka topic
    private boolean async; // true — Kafka, false — direct
}

