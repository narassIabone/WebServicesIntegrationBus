package org.example.model.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class AuditData {
    private final UUID traceId;
    private final UUID messageId;
    private final UUID routeId;
    private final Integer nodeId;
    private final String nodeType;
    private final String status;
    private final String payloadBefore;
    private final String payloadAfter;
    private final Map<String, Object> headers;
    private final Map<String, Object> context;
    private final String errorMessage;
    private final Long duration;
}