package org.example.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Table(name = "route_ui_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteUiMetadata {

    @Id
    @Column(name = "route_id")
    private UUID routeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_data", columnDefinition = "jsonb")
    private JsonNode layoutData;
}
