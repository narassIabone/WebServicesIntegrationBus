package org.example.model;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;


@Entity
@Table(name = "messages")
public class Message {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "payload")
    private String payload;

    //@Column(name = "headers")
    // Map<String, String> headers;

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

//    public Map<String, String> getHeaders() {
//        return headers;
//    }

//    public void setHeaders(Map<String, String> headers) {
//        this.headers = headers;
//    }

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
