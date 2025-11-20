package org.example.model.route;

import java.util.List;

public class RouteConfig {
    private String routeId;
    private String description;
    private List<NodeConfig> nodes;
    private List<RouteLink> links;

    public RouteConfig() {}

    public RouteConfig(String routeId, String description, List<NodeConfig> nodes, List<RouteLink> links) {
        this.routeId = routeId;
        this.description = description;
        this.nodes = nodes;
        this.links = links;
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

    public List<RouteLink> getLinks() {
        return links;
    }

    public void setLinks(List<RouteLink> links) {
        this.links = links;
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
