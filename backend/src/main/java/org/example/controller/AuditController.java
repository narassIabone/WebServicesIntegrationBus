package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.dto.RouteStatDTO;
import org.example.model.dto.MessageAudit;
import org.example.repository.MessageAuditRepository;
import org.example.service.TimeRangeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditController {

    private final MessageAuditRepository auditRepository;
    private final TimeRangeService timeRangeService;

    @GetMapping("/stats/details")
    public Map<String, Object> getFullStats(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        var dates = timeRangeService.getDates(range, start, end);

        // Получаем агрегаты по маршрутам (отсюда возьмем AVG Latency)
        List<Object[]> routeData = auditRepository.getRouteAnalyticsData(dates.from(), dates.to());

        // 1. Честный Summary: Total и Errors считаем по TraceId
        long total = auditRepository.countInRange(dates.from(), dates.to());
        long errors = auditRepository.countByStatusInRange("ERROR", dates.from(), dates.to());
        long success = Math.max(0, total - errors);

        // 2. Средняя задержка по всем маршрутам
        double avgLatency = routeData.stream()
                .mapToDouble(row -> ((Number) row[3]).doubleValue())
                .average()
                .orElse(0.0);

        // 3. График почасовой активности
        List<Object[]> rawStats = auditRepository.getHourlyStatsInRange(dates.from(), dates.to());
        List<Map<String, Object>> formattedStats = rawStats.stream().map(row -> {
            LocalDateTime ldt = parseToLocalDateTime(row[0]);
            return Map.of(
                    "time", ldt.format(DateTimeFormatter.ofPattern("dd.MM HH:mm")),
                    "success", row[1] != null ? row[1] : 0,
                    "errors", row[2] != null ? row[2] : 0
            );
        }).collect(Collectors.toList());

        return Map.of(
                "summary", Map.of("total", total, "success", success, "errors", errors),
                "avgLatency", Math.round(avgLatency),
                "nodeErrors", auditRepository.countErrorsByNodeTypeInRange(dates.from(), dates.to()),
                "hourlyStats", formattedStats
        );
    }

    @GetMapping("/stats/routes")
    public List<RouteStatDTO> getRouteAnalytics(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        var dates = timeRangeService.getDates(range, start, end);
        List<Object[]> results = auditRepository.getRouteAnalyticsData(dates.from(), dates.to());

        return results.stream().map(row -> {
            String routeName = (row[0] != null) ? (String) row[0] : "Unknown Route";
            long total = ((Number) row[1]).longValue();
            long errors = ((Number) row[2]).longValue();
            double avgLat = ((Number) row[3]).doubleValue();

            long health = total > 0 ? ((total - errors) * 100) / total : 100;

            return new RouteStatDTO(null, routeName, total, errors, health, Math.round(avgLat));
        }).collect(Collectors.toList());
    }

    @GetMapping("/incidents")
    public List<MessageAudit> getRecentIncidents(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        var dates = timeRangeService.getDates(range, start, end);
        return auditRepository.findAllByStatusAndCreatedAtBetweenOrderByCreatedAtDesc("ERROR", dates.from(), dates.to());
    }

    @GetMapping("/messages")
    public Map<String, Object> getRecentMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String route,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) String search) {

        int offset = (page - 1) * limit;

        List<Map<String, Object>> messages = auditRepository.findFilteredTraces(route, start, end, search, limit, offset);
        long totalCount = auditRepository.countFilteredTraces(route, start, end, search);

        return Map.of(
                "messages", messages,
                "totalCount", totalCount
        );
    }

    @GetMapping("/trace/{traceId}")
    public List<MessageAudit> getTraceDetails(@PathVariable UUID traceId) {
        return auditRepository.findAllByTraceIdOrderByCreatedAtAsc(traceId);
    }

    private LocalDateTime parseToLocalDateTime(Object obj) {
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
        if (obj instanceof java.time.Instant) return LocalDateTime.ofInstant((java.time.Instant) obj, java.time.ZoneId.systemDefault());
        if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
        return LocalDateTime.now();
    }
}
