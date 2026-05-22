package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.AuditData;
import org.example.model.dto.MessageAudit;
import org.example.repository.MessageAuditRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final MessageAuditRepository auditRepository;

    @Async
    public void logStep(AuditData data) {
        try {
            MessageAudit audit = MessageAudit.builder()
                    .id(UUID.randomUUID())
                    .traceId(data.getTraceId())
                    .messageId(data.getMessageId())
                    .routeId(data.getRouteId())
                    .routeName(data.getRouteName())
                    .nodeId(data.getNodeId())
                    .nodeType(data.getNodeType())
                    .status(data.getStatus())
                    .payloadBefore(data.getPayloadBefore())
                    .payloadAfter(data.getPayloadAfter())
                    .headersSnapshot(data.getHeaders())
                    .contextSnapshot(data.getContext())
                    .nodeConfig(data.getNodeConfig())
                    .errorMessage(data.getErrorMessage())
                    .executionTimeMs(data.getDuration())
                    .build();

            auditRepository.save(audit);
        } catch (Exception e) {
            log.error("[Audit Error] Не удалось сохранить шаг аудита для сообщения {}: {}",
                    data.getMessageId(), e.getMessage());
        }
    }
}