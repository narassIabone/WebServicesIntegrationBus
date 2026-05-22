package org.example.model.dto;

import java.util.UUID;

public record RouteStatDTO(
        UUID id,
        String name,
        long count,
        long errors,
        long health,
        long avgLatency
) {}
