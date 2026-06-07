package org.example.model.dto;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "message_audit")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class MessageAudit {

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Id
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Column(name = "route_name")
    private String routeName;

    @Column(name = "trace_id")
    private UUID traceId;

    @Column(name = "node_id", nullable = false)
    private Integer nodeId;

    @Column(name = "node_name")
    private String nodeName;
    

    @Column(name = "node_type")
    private String nodeType;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String payloadBefore;

    @Column(columnDefinition = "TEXT")
    private String payloadAfter;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers_snapshot")
    private Map<String, Object> headersSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_snapshot")
    private Map<String, Object> contextSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "node_config")
    private Map<String, Object> nodeConfig;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
