package org.example.model.route;

import java.util.List;

public class RouteConfig {
    private String Id;
    private String startTopic;
    private List<NodeConfig> nodes;
    private List<RouteLink> links;

    public RouteConfig() {}

    public RouteConfig(String routeId, String description, List<NodeConfig> nodes, List<RouteLink> links) {
        this.Id = routeId;
        this.startTopic = description;
        this.nodes = nodes;
        this.links = links;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getStartTopic() {
        return startTopic;
    }

    public void setStartTopic(String startTopic) {
        this.startTopic = startTopic;
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
                "routeId='" + Id + '\'' +
                ", description='" + startTopic + '\'' +
                ", nodes=" + nodes +
                '}';
    }
}
