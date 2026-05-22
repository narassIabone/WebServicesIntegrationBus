package org.example.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RouteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "route_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "active")
    private boolean isActive;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "route_id")
    private List<NodeConfig> nodes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "route_id")
    private List<RouteLink> links;
}