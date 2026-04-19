package org.example.engine.executor;

import jakarta.annotation.PostConstruct;
import org.example.model.route.RouteConfig;
import org.example.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RouteCacheService {

    private static final Logger log = LoggerFactory.getLogger(RouteCacheService.class);

    private final RouteRepository routeRepository;

    private final Map<UUID, RouteConfig> cache = new ConcurrentHashMap<>();

    public RouteCacheService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Загрузка кэша при старте приложения.
     */
    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * Синхронизация кэша с БД.
     */
    public void refresh() {
        log.info("[Cache] Synchronizing routes from database...");
        try {
            List<RouteConfig> routesFromDb = routeRepository.findAll();

            Map<UUID, RouteConfig> freshData = new ConcurrentHashMap<>();
            routesFromDb.forEach(route -> freshData.put(route.getId(), route));

            cache.keySet().retainAll(freshData.keySet());
            cache.putAll(freshData);

            log.info("[Cache] Successfully loaded {} routes.", cache.size());
        } catch (Exception e) {
            log.error("[Cache] Failed to load routes: {}", e.getMessage());
        }
    }

    /**
     * Получение маршрута.
     * Теперь принимает UUID для строгого соответствия типам.
     */
    public RouteConfig getRoute(UUID routeId) {
        if (routeId == null) return null;
        return cache.get(routeId);
    }

    /**
     * Проверка существования маршрута.
     */
    public boolean containsRoute(UUID routeId) {
        return routeId != null && cache.containsKey(routeId);
    }

    /**
     * Метод для отладки: посмотреть все загруженные ID.
     */
    public void printCacheStatus() {
        log.info("[Cache] Current IDs in cache: {}", cache.keySet());
    }
}