package dev.code_offline.basalt.view;

import dev.code_offline.basalt.model.Node;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.Random;

public class Graph extends JPanel implements MouseListener, MouseMotionListener {
    private final int NODE_SIZE = 25;

    private final World<Body> world = new World<>();

    private final List<Node> nodes;

    public Graph(List<Node> nodes) {
        this.nodes = nodes;

        world.setGravity(0, 0);

        addMouseListener(this);
        addMouseMotionListener(this);

        nodes.forEach(node -> {
            var body = node.getBody();
            var random = new Random();

            body.translate(random.nextInt(500), random.nextInt(500));
            body.addFixture(Geometry.createCircle(NODE_SIZE));
            body.setMass(MassType.NORMAL);
            world.addBody(body);
        });
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        nodes.forEach(node -> {
            int x = (int) node.getBody().getWorldCenter().x;
            int y = (int) node.getBody().getWorldCenter().y;

            graphics.fillOval(x, y, NODE_SIZE, NODE_SIZE);

            graphics.drawString(node.getName(), x, y);
            graphics.drawString(node.getAuthor(), x, (int) (y + NODE_SIZE * 1.5));

            var parent = node.getParent();

            var nodeOffset = NODE_SIZE / 2;

            if (parent != null) {
                var parentX = (int) parent.getBody().getWorldCenter().x;
                var parentY = (int) parent.getBody().getWorldCenter().y;

                graphics.drawLine(x + nodeOffset, y + nodeOffset, parentX + nodeOffset, parentY + nodeOffset);
            }
        });

        world.step(1);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        nodes.forEach(node -> {
        });
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        repaint();
    }
}
