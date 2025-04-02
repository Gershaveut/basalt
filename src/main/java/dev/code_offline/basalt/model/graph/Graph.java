package dev.code_offline.basalt.model.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Graph {
    private final List<Node> nodes;

    public Graph() {
        this.nodes = new ArrayList<>();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void initializeSampleData() {
        for (int i = 0; i < 25; i++) {
            var random = new Random();
            var linkList = new ArrayList<Node>();

            if (random.nextInt(3) == 1 && nodes.size() > 1)
                linkList.add(nodes.get(random.nextInt(nodes.size() - 1)));

            nodes.add(new NodeElement("Test", "Gershaveut", linkList));
        }
    }
}