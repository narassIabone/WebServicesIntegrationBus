package org.example.repository;

import org.example.model.dto.MessageAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface MessageAuditRepository extends JpaRepository<MessageAudit, UUID> {

    // Поиск конкретных записей со статусом ERROR
    List<MessageAudit> findAllByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            String status, LocalDateTime from, LocalDateTime to
    );

    // Счётчик уникальных цепочек (сообщений)
    @Query("SELECT COUNT(DISTINCT m.traceId) FROM MessageAudit m WHERE m.createdAt BETWEEN :from AND :to")
    long countInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Счётчик цепочек, содержащих хотя бы одну ошибку
    @Query("SELECT COUNT(DISTINCT m.traceId) FROM MessageAudit m WHERE m.status = :status AND m.createdAt BETWEEN :from AND :to")
    long countByStatusInRange(@Param("status") String status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Статистика ошибок по типам узлов (для BarChart)
    @Query("SELECT m.nodeType, COUNT(m) FROM MessageAudit m WHERE m.status = 'ERROR' " +
            "AND m.createdAt BETWEEN :from AND :to GROUP BY m.nodeType")
    List<Object[]> countErrorsByNodeTypeInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Почасовая статистика (универсально для Postgres)
    @Query(value = "SELECT date_trunc('hour', created_at) as hr, " +
            "count(DISTINCT trace_id) filter (where status = 'SUCCESS') as success, " +
            "count(DISTINCT trace_id) filter (where status = 'ERROR') as errors " +
            "FROM message_audit " +
            "WHERE created_at BETWEEN :from AND :to " +
            "GROUP BY hr ORDER BY hr", nativeQuery = true)
    List<Object[]> getHourlyStatsInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Главный метод аналитики (считает всё за один проход по индексам)
    @Query(value = """
    SELECT 
        route_name,
        COUNT(trace_id) as total_messages,
        SUM(has_error) as total_errors,
        AVG(total_latency) as avg_route_latency
    FROM (
        SELECT 
            route_name, 
            trace_id,
            MAX(CASE WHEN status = 'ERROR' THEN 1 ELSE 0 END) as has_error,
            SUM(execution_time_ms) as total_latency
        FROM message_audit
        WHERE created_at BETWEEN :from AND :to
        GROUP BY route_name, trace_id
    ) as trace_summaries
    GROUP BY route_name
    """, nativeQuery = true)
    List<Object[]> getRouteAnalyticsData(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Поиск для таблицы мониторинга (DISTINCT ON гарантирует одну строку на один trace_id)
    @Query(value = """
    SELECT * FROM (
        SELECT DISTINCT ON (trace_id)
            trace_id as "traceId", 
            route_name as "routeName", 
            status, 
            created_at as "createdAt"
        FROM message_audit
        WHERE (:route IS NULL OR :route = 'ALL' OR route_name = :route)
          AND (:search IS NULL OR CAST(trace_id AS TEXT) ILIKE CONCAT('%', :search, '%'))
          AND (:start IS NULL OR :start = '' OR created_at >= CAST(:start AS TIMESTAMP))
          AND (:end IS NULL OR :end = '' OR created_at <= CAST(:end AS TIMESTAMP))
        ORDER BY trace_id, created_at DESC
    ) as subquery
    ORDER BY "createdAt" DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Map<String, Object>> findFilteredTraces(
            @Param("route") String route, @Param("start") String start, @Param("end") String end,
            @Param("search") String search, @Param("limit") int limit, @Param("offset") int offset
    );

    @Query(value = """
    SELECT COUNT(DISTINCT trace_id)
    FROM message_audit
    WHERE (:route IS NULL OR :route = 'ALL' OR route_name = :route)
      AND (:search IS NULL OR CAST(trace_id AS TEXT) ILIKE CONCAT('%', :search, '%'))
      AND (:start IS NULL OR :start = '' OR created_at >= CAST(:start AS TIMESTAMP))
      AND (:end IS NULL OR :end = '' OR created_at <= CAST(:end AS TIMESTAMP))
    """, nativeQuery = true)
    long countFilteredTraces(
            @Param("route") String route, @Param("start") String start, @Param("end") String end, @Param("search") String search
    );

    List<MessageAudit> findAllByTraceIdOrderByCreatedAtAsc(UUID traceId);
}