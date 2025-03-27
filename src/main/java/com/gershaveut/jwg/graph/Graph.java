package com.gershaveut.jwg.graph;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class Graph extends JPanel {
    private final int NODE_SIZE = 25;

    private final List<NodeElement> nodeElements;

    public Graph(List<NodeInfo> nodeInfos) {
        this.nodeElements = nodeInfos.stream().map(NodeElement::new).toList();

        nodeElements.forEach(nodeElement -> {
            var random = new Random();

            nodeElement.position.setLocation(random.nextInt(500), random.nextInt(500));
        });
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        nodeElements.forEach(nodeElement -> {
            graphics.fillOval(nodeElement.position.getXInt(), nodeElement.position.getYInt(), NODE_SIZE, NODE_SIZE);

            var parent = nodeElement.getParent();

            if (parent != null) {
                var nodeOffset = NODE_SIZE / 2;

                graphics.drawLine(nodeElement.position.getXInt() + nodeOffset, nodeElement.position.getYInt() + nodeOffset,
                        parent.getPosition().getXInt() + nodeOffset, parent.getPosition().getYInt() + nodeOffset);
            }
        });
    }
}
