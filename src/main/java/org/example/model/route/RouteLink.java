package org.example.model.route;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "links")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "link_id", updatable = false, nullable = false)
    private UUID Id;

    private int fromNodeId;
    private int toNodeId;

    private String outputTopic;
    private boolean async;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "link_field_mappings",
            joinColumns = @JoinColumn(name = "link_id")
    )
    @MapKeyColumn(name = "source_field")
    @Column(name = "target_field")
    private Map<String, String> fieldMapping;
}