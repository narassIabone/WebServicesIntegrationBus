package org.example.repository;

import org.example.model.entity.MessageAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageAuditRepository extends JpaRepository<MessageAudit, UUID> {
    List<MessageAudit> findAllByTraceIdOrderByCreatedAtAsc(UUID traceId);

    long countByStatus(String status);
}
