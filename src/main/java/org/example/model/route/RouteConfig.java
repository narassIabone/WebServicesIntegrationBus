package org.example.model.route;

import java.util.List;

public class RouteConfig {
    private String routeId;
    private String description;
    private List<NodeConfig> nodes;

    public RouteConfig() {}

    public RouteConfig(String routeId, String description, List<NodeConfig> nodes) {
        this.routeId = routeId;
        this.description = description;
        this.nodes = nodes;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<NodeConfig> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeConfig> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "RouteConfig{" +
                "routeId='" + routeId + '\'' +
                ", description='" + description + '\'' +
                ", nodes=" + nodes +
                '}';
    }
}
