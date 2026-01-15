package org.example.model.core;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers", columnDefinition = "jsonb")
    private Map<String, Object> headers = new HashMap<>();

    @Transient
    private Map<String, Object> context = new HashMap<>();

    public Message() {
        // Пустой конструктор для Hibernate
    }

    public Message(String payload) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.status = MessageStatus.NEW;
        this.payload = payload;
    }

    public Message(String payload, Map<String, Object> headers) {
        this(payload);
        if (headers != null) {
            this.headers.putAll(headers);
        }
    }

    // --- Геттеры и сеттеры ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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

    // --- Бизнес-методы для удобства ---

    public void setRouteId(String routeId) {
        this.context.put("INTERNAL_ROUTE_ID", routeId);
    }

    public String getRouteId() {
        return (String) this.context.get("INTERNAL_ROUTE_ID");
    }

    public void copyContextFrom(Message original) {
        if (original != null && original.getContext() != null) {
            this.context.putAll(original.getContext());
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", headers=" + headers +
                '}';
    }
}