package org.example.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TimeRangeService {
    public RangeDates getDates(String range, String start, String end) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime from = switch (range != null ? range : "day") {
            case "hour" -> now.minusHours(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "custom" -> (start != null) ? LocalDateTime.parse(start) : now.minusDays(1);
            case "all" -> LocalDateTime.of(2000, 1, 1, 0, 0);
            default -> now.minusDays(1); // "day"
        };

        LocalDateTime to = (range.equals("custom") && end != null)
                ? LocalDateTime.parse(end)
                : now;

        return new RangeDates(from, to);
    }

    public record RangeDates(LocalDateTime from, LocalDateTime to) {}
}
