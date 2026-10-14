package org.example.engine.executor;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.model.entity.RouteConfig;
import org.example.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RouteCacheService {

    private final RouteRepository routeRepository;
    private final Map<UUID, RouteConfig> cache = new ConcurrentHashMap<>();

    public RouteCacheService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        log.info("[Cache] Синхронизация маршрутов из базы данных...");
        try {
            List<RouteConfig> routesFromDb = routeRepository.findAll();

            Map<UUID, RouteConfig> freshData = new ConcurrentHashMap<>();
            routesFromDb.stream()
                    .filter(RouteConfig::isActive)
                    .forEach(route -> freshData.put(route.getId(), route));

            cache.keySet().retainAll(freshData.keySet());
            cache.putAll(freshData);

            log.info("[Cache] Успешно загружено активных маршрутов: {}", cache.size());
        } catch (Exception e) {
            log.error("[Error] Не удалось обновить кэш маршрутов: {}", e.getMessage());
        }
    }

    public RouteConfig getRoute(UUID routeId) {
        if (routeId == null) return null;

        RouteConfig route = cache.get(routeId);
        if (route == null) {
            // Уровень warn, так как запрос несуществующего маршрута — это подозрительно
            log.warn("[Cache] Запрошен отсутствующий маршрут с ID: {}", routeId);
        }
        return route;
    }

    public boolean containsRoute(UUID routeId) {
        return routeId != null && cache.containsKey(routeId);
    }

    public void printCacheStatus() {
        log.info("[Cache] Текущие ID в кэше: {}", cache.keySet());
    }
}