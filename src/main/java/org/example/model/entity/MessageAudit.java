package org.example.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "message_audit")
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class MessageAudit {

    @Id
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Column(name = "trace_id")
    private UUID traceId;

    @Column(name = "node_id", nullable = false)
    private Integer nodeId;

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

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
