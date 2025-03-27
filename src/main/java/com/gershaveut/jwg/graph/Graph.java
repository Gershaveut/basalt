package com.gershaveut.jwg.graph;

import com.gershaveut.jwg.util.Vector2DInt;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class Graph extends JPanel {
    private final int NODE_SIZE = 25;

    private final List<Node> nodes;

    public Graph(List<Node> nodes) {
        this.nodes = nodes;

        nodes.forEach(node -> {
            var random = new Random();

            node.setPosition(new Vector2DInt(random.nextInt(500), random.nextInt(500)));
        });
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        nodes.forEach(nodeElement -> {
            graphics.fillOval(nodeElement.getPosition().getXInt(), nodeElement.getPosition().getYInt(), NODE_SIZE, NODE_SIZE);

            var parent = nodeElement.getParent();

            if (parent != null) {
                var nodeOffset = NODE_SIZE / 2;

                graphics.drawLine(nodeElement.getPosition().getXInt() + nodeOffset, nodeElement.getPosition().getYInt() + nodeOffset,
                        parent.getPosition().getXInt() + nodeOffset, parent.getPosition().getYInt() + nodeOffset);
            }
        });
    }
}
