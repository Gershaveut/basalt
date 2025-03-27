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

        nodes.forEach(node -> {
            var x = node.getPosition().getXInt();
            var y = node.getPosition().getYInt();

            graphics.fillOval(x, y, NODE_SIZE, NODE_SIZE);

            graphics.drawString(node.getName(), x, y);
            graphics.drawString(node.getAuthor(), x, (int) (y + NODE_SIZE * 1.5));

            var parent = node.getParent();

            var nodeOffset = NODE_SIZE / 2;

            if (parent != null) {
                var parentX = parent.getPosition().getXInt();
                var parentY = parent.getPosition().getYInt();

                graphics.drawLine(x + nodeOffset, y + nodeOffset, parentX + nodeOffset, parentY + nodeOffset);
            }
        });
    }
}
