package org.example.repository;

import org.example.model.entity.RouteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<RouteConfig, UUID> {
    Optional<RouteConfig> findByName(String name);
}