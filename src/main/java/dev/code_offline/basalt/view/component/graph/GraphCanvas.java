package dev.code_offline.basalt.view.component.graph;

import dev.code_offline.basalt.Util;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.StepListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GraphCanvas extends JComponent implements MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener, StepListener<Body> {
    private final double NANO_TO_BASE = 1.0e9;
    private final int NODE_SIZE = 25;
    private final int MOVE_GRAPH = MouseEvent.BUTTON2;
    private final int MOVE_NODE = MouseEvent.BUTTON1;

    // настройки физики
    private final Vector2 GRAVITY = new Vector2();
    private final double DAMPING = 0.5;
    private final int FORCE_POWER = 1000;

    public final World<Body> world = new World<>();
    public final List<Node> nodes;

    private Point lastMousePos;
    private Vector2 offset = new Vector2();

    private double scale = 1.0;

    private Node draggedNode;

    private final Thread physicThread;
    private long last;

    public GraphCanvas(List<Node> nodes) {
        this.nodes = nodes;

        world.setGravity(GRAVITY);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addComponentListener(this);
        this.addMouseWheelListener(this);

        initializeNodes();

        physicThread = new Thread(() -> {
            while (true) {
                // расчёт сколько секунд прошло с последнего вызова
                long time = System.nanoTime();

                long diff = time - this.last;
                this.last = time;
                double elapsedTime = (double) diff / NANO_TO_BASE;

                world.update(elapsedTime);

                try {
                    // освобождение процессора
                    Thread.sleep(5);
                } catch (InterruptedException ignored) {
                }
            }
        });

        world.addStepListener(this);
        physicThread.start();
    }

    private void initializeNodes() {
        nodes.forEach(node -> {
            var body = node.getBody();
            var random = new Random();

            body.translate(random.nextInt(500), random.nextInt(500));
            body.addFixture(Geometry.createCircle(NODE_SIZE));
            body.setMass(MassType.NORMAL);

            body.setLinearDamping(DAMPING);
            body.setAngularDamping(DAMPING);

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

            offset.x = ((getWidth() / scale - graphWidth) / 2 - minX);
            offset.y = ((getHeight() / scale - graphHeight) / 2 - minY);
        }
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate((int) offset.x, (int) offset.y);
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

        g2d.dispose();
    }

    // перемещение области
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MOVE_GRAPH) {
            lastMousePos = e.getPoint();
        } else if (e.getButton() == MOVE_NODE) {
            nodes.forEach(node -> {
                if (node.getBody().getWorldCenter().distance(getMouseWorldPosition()) <= NODE_SIZE) {
                    draggedNode = node;
                }
            });
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MOVE_GRAPH) {
            lastMousePos = null;
        } else if (e.getButton() == MOVE_NODE) {
            draggedNode = null;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastMousePos != null) {
            offset.x += (e.getX() - lastMousePos.x);
            offset.y += (e.getY() - lastMousePos.y);
            lastMousePos = e.getPoint();
        }
    }

    // отслеживание изменения размера окна для центрирования графа
    @Override
    public void componentResized(ComponentEvent e) {
        if (getWidth() > 0 && getHeight() > 0) {
            centerGraph();
        }
    }

    // масштабирование
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double mouseX = e.getX();
        double mouseY = e.getY();

        double graphMouseX = (mouseX - offset.x) / scale;
        double graphMouseY = (mouseY - offset.y) / scale;

        double wheelDelta = e.getPreciseWheelRotation();
        double scaleFactor = Math.pow(1.1, -wheelDelta);
        scale *= scaleFactor;

        scale = Math.max(0.3, Math.min(5.0, scale));

        offset.x = mouseX - graphMouseX * scale;
        offset.y = mouseY - graphMouseY * scale;

    }

    @Override
    public void end(TimeStep step, PhysicsWorld world) {
        SwingUtilities.invokeLater(() -> {
            repaint();

            // перемещение ноды
            if (draggedNode != null) {
                Vector2 target = getMouseWorldPosition();

                if (target == null)
                    return;

                Vector2 current = draggedNode.getBody().getWorldCenter();
                Vector2 force = target.subtract(current).multiply(FORCE_POWER);

                draggedNode.getBody().applyForce(force);
            }
        });
    }

    @Override
    public void mouseMoved(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void componentMoved(ComponentEvent e) {}
    @Override
    public void componentShown(ComponentEvent e) {}
    @Override
    public void componentHidden(ComponentEvent e) {}
    @Override
    public void begin(TimeStep step, PhysicsWorld world) {}
    @Override
    public void updatePerformed(TimeStep step, PhysicsWorld world) {}
    @Override
    public void postSolve(TimeStep step, PhysicsWorld world) {}

    // возврощает позицию мыши в мировом представлении
    public Vector2 getMouseWorldPosition() {
        if (getMousePosition() == null)
            return null;

        return Util.pointToVector(getMousePosition()).subtract(offset).divide(scale);
    }

    public Vector2 getOffset() {
        return offset;
    }

    public double getScale() {
        return scale;
    }
}
