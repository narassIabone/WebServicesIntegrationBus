package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.entity.MessageAudit;
import org.example.repository.MessageAuditRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditController {

    private final MessageAuditRepository auditRepository;

    @GetMapping("/trace/{traceId}")
    public List<MessageAudit> getTraceDetails(@PathVariable UUID traceId) {
        return auditRepository.findAllByTraceIdOrderByCreatedAtAsc(traceId);
    }

    @GetMapping("/stats/summary")
    public Map<String, Long> getStatsSummary() {
        return Map.of(
                "total", auditRepository.count(),
                "success", auditRepository.countByStatus("SUCCESS"),
                "errors", auditRepository.countByStatus("ERROR")
        );
    }
}
