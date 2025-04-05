package dev.code_offline.basalt.model.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Graph {
    private final List<Node> nodes;

    public Graph() {
        nodes = new ArrayList<>();
    }

    public Graph(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}