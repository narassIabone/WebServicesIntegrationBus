package org.example.repository;

import org.example.model.dto.RouteUiMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RouteUiMetadataRepository extends JpaRepository<RouteUiMetadata, UUID> {
}