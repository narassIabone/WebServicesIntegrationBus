package org.example.model.route;

import jakarta.persistence.*;
import lombok.*;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "nodes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NodeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "node_id", updatable = false, nullable = false)
    private UUID technicalId;

    @Column(name = "business_id")
    private int id;

    @Enumerated(EnumType.STRING)
    private NodeType type;

    private boolean isStart;

    @Column(name = "input_topic")
    private String inputTopic;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "node_config_params",
            joinColumns = @JoinColumn(name = "node_id")
    )
    @MapKeyColumn(name = "param_key")
    @Column(name = "param_value")
    private Map<String, String> config;
}