package org.gershaveut.basalt.model.graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final List<Node> nodes;

    public Graph(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Graph() {
        this(new ArrayList<>());
    }

    public List<Node> getNodes() {
        return nodes;
    }
}