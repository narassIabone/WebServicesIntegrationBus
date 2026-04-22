package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.engine.executor.RouteAdminService;
import org.example.engine.executor.RouteCacheService;
import org.example.model.route.RouteConfig;
import org.example.repository.RouteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/routes")
@RequiredArgsConstructor
public class RouteAdminController {

    private final RouteRepository routeRepository;
    private final RouteCacheService routeCacheService;
    private final RouteAdminService routeAdminService;

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
}