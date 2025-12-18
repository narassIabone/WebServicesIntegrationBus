package org.example.model.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Message {

    private final String id;
    private final Instant createdAt;
    private MessageStatus status;
    private String payload;
    private Map<String, Object> headers = new HashMap<>();
    private Map<String, Object> context = new HashMap<>();

    // Конструктор
    public Message(String payload) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.status = MessageStatus.NEW;
        this.payload = payload;
    }

    // Конструктор с заголовками
    public Message(String payload, Map<String, Object> headers) {
        this(payload);
        if (headers != null) {
            this.headers.putAll(headers);
        }
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public void setRouteId(String routeId) {
        this.context.put("INTERNAL_ROUTE_ID", routeId);
    }

    public String getRouteId() {
        return (String) this.context.get("INTERNAL_ROUTE_ID");
    }

    public void copyContextFrom(Message original) {
        this.context.putAll(original.context);
    }
}