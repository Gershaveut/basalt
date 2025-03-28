package dev.code_offline.basalt.view;

import dev.code_offline.basalt.model.Node;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Graph extends JPanel implements MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener {
    private final int NODE_SIZE = 25;

    private final World<Body> world = new World<>();

    private final List<Node> nodes;

    private Point lastMousePos;
    private double offsetX = 0, offsetY = 0;
    private double scale = 1.0;

    public Graph(List<Node> nodes) {
        this.nodes = nodes;

        world.setGravity(0, 0);

        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        addMouseWheelListener(this);

        initializeNodes();
    }

    private void initializeNodes() {
        nodes.forEach(node -> {
            var body = node.getBody();
            var random = new Random();

            body.translate(random.nextInt(700), random.nextInt(700));
            body.addFixture(Geometry.createCircle(NODE_SIZE));
            body.setMass(MassType.NORMAL);
            world.addBody(body);
        });
    }

    private void centerGraph() {
        if (!nodes.isEmpty()) {
            var arrayX = nodes.stream().mapToDouble(n -> n.getBody().getWorldCenter().x).toArray();
            var arrayY = nodes.stream().mapToDouble(n -> n.getBody().getWorldCenter().y).toArray();

            double minX = Arrays.stream(arrayX).min().orElseThrow();
            double maxX = Arrays.stream(arrayX).max().orElseThrow();

            double minY = Arrays.stream(arrayY).min().orElseThrow();
            double maxY = Arrays.stream(arrayY).max().orElseThrow();

            double graphWidth = maxX - minX;
            double graphHeight = maxY - minY;

            offsetX = ((getWidth() / scale - graphWidth) / 2 - minX);
            offsetY = ((getHeight() / scale - graphHeight) / 2 - minY);
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate((int) offsetX, (int) offsetY);
        g2d.scale(scale, scale);

        nodes.forEach(node -> {
            int x = (int) node.getBody().getWorldCenter().x;
            int y = (int) node.getBody().getWorldCenter().y;

            g2d.fillOval(x, y, NODE_SIZE, NODE_SIZE);

            g2d.drawString(node.getName(), x, y);
            g2d.drawString(node.getAuthor(), x, (int) (y + NODE_SIZE * 1.5));

            var parent = node.getParent();

            var nodeOffset = NODE_SIZE / 2;

            if (parent != null) {
                var parentX = (int) parent.getBody().getWorldCenter().x;
                var parentY = (int) parent.getBody().getWorldCenter().y;

                g2d.drawLine(x + nodeOffset, y + nodeOffset, parentX + nodeOffset, parentY + nodeOffset);
            }
        });

        world.step(1);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    // перемещение области
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            lastMousePos = e.getPoint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            lastMousePos = null;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastMousePos != null) {
            offsetX += (e.getX() - lastMousePos.x);
            offsetY += (e.getY() - lastMousePos.y);
            lastMousePos = e.getPoint();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    // отслеживание изменения размера окна для центрирования графа
    // TODO: при изменение размера окна ВСЕГДА происходит центрирование, нужно ли это?
    @Override
    public void componentResized(ComponentEvent e) {
        if (getWidth() > 0 && getHeight() > 0) {
            centerGraph();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    // масштабирование
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double mouseX = e.getX();
        double mouseY = e.getY();

        double graphMouseX = (mouseX - offsetX) / scale;
        double graphMouseY = (mouseY - offsetY) / scale;

        double wheelDelta = e.getPreciseWheelRotation();
        double scaleFactor = Math.pow(1.1, -wheelDelta);
        scale *= scaleFactor;

        scale = Math.max(0.3, Math.min(5.0, scale));

        offsetX = mouseX - graphMouseX * scale;
        offsetY = mouseY - graphMouseY * scale;

        repaint();
    }
}
