package org.example.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;


@Entity
@Table(name = "messages")
public class Message {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "payload")
    private String payload;

    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;

    public Message(String payload
            //, Map<String, String> headers
                   ) {
        this.id = UUID.randomUUID().toString();
        this.payload = payload;
        //this.headers = headers;
        this.timestamp = LocalDateTime.now();
        this.status = MessageStatus.NEW; // начальный статус
    }

    public String getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, String> getHeaders() {
        if (headers == null) return null;
        try {
            return new ObjectMapper().readValue(headers, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setHeaders(Map<String, String> headers) {
        try {
            this.headers = new ObjectMapper().writeValueAsString(headers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
