package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.example.engine.executor.RouteAdminService;
import org.example.engine.executor.RouteCacheService;
import org.example.model.dto.RouteUiMetadata;
import org.example.model.entity.RouteConfig;
import org.example.repository.RouteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.repository.RouteUiMetadataRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/routes")
@RequiredArgsConstructor
public class RouteAdminController {

    private final RouteRepository routeRepository;
    private final RouteCacheService routeCacheService;
    private final RouteAdminService routeAdminService;
    private final RouteUiMetadataRepository uiMetadataRepository;

    @PostMapping
    public ResponseEntity<RouteConfig> saveRoute(@RequestBody RouteConfig routeConfig) {
        if (routeConfig.getId() == null) routeConfig.setId(UUID.randomUUID());

        routeAdminService.prepareRouteConfig(routeConfig);
        RouteConfig saved = routeRepository.save(routeConfig);
        routeCacheService.refresh();
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<RouteConfig> getAllRoutes() {
        return routeRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable UUID id) {
        routeRepository.deleteById(id);
        routeCacheService.refresh();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cache/refresh")
    public ResponseEntity<String> refreshCache() {
        routeCacheService.refresh();
        return ResponseEntity.ok("Cache refreshed successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteConfig> updateRoute(@PathVariable UUID id, @RequestBody RouteConfig routeConfig) {
        if (!routeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        routeConfig.setId(id);
        routeAdminService.prepareRouteConfig(routeConfig);
        RouteConfig updated = routeRepository.save(routeConfig);
        routeCacheService.refresh();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{routeId}/metadata")
    public ResponseEntity<JsonNode> getMetadata(@PathVariable UUID routeId) {
        return uiMetadataRepository.findById(routeId)
                .map(meta -> ResponseEntity.ok(meta.getLayoutData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{routeId}/metadata")
    public ResponseEntity<Void> saveMetadata(
            @PathVariable UUID routeId,
            @RequestBody JsonNode layoutData) {

        RouteUiMetadata metadata = new RouteUiMetadata(routeId, layoutData);
        uiMetadataRepository.save(metadata);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteConfig> getRoute(@PathVariable UUID id) {
        return routeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleRouteStatus(
            @PathVariable UUID id,
            @RequestParam boolean active) { // Параметр в URL: ?active=true

        routeRepository.findById(id).ifPresent(route -> {
            route.setActive(active); // Lombok для boolean isActive генерирует setActive
            routeRepository.save(route);
            routeCacheService.refresh();
        });

        return ResponseEntity.ok().build();
    }
}