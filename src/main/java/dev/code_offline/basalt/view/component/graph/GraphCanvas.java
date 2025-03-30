package dev.code_offline.basalt.view.component.graph;

import dev.code_offline.basalt.Util;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.dynamics.joint.DistanceJoint;
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

public class GraphCanvas extends AbstractButton implements MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener, StepListener<Body> {
    private final double NANO_TO_BASE = 1.0e9;

    private final int NODE_SIZE = 25;
    private final int MOVE_GRAPH = MouseEvent.BUTTON3;
    private final int MOVE_NODE = MouseEvent.BUTTON1;

    // настройки физики
    private final Vector2 GRAVITY = new Vector2();
    private final MassType NODE_MASS = MassType.NORMAL;
    private final double DAMPING = 0.5;
    private final double REST_DISTANCE = 150;
    private final double SPRING_FREQUENCY = 8;
    public final World<Body> world = new World<>();

    public List<Node> nodes;

    private Point lastMousePos;
    private Vector2 offset = new Vector2();

    private double scale = 1.0;

    private Node draggedNode;

    private boolean physicThreadLive; // нужно что-бы дать знать когда потоку на покой
    private final Runnable physicThreadRun;
    private Thread physicThread;
    private long last;

    public GraphCanvas(List<Node> nodes) {
        this.nodes = nodes;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addComponentListener(this);
        this.addMouseWheelListener(this);

        physicThreadRun = () -> {
            physicThreadLive = true;

            while (physicThreadLive) { // если false поток завершит работу
                if (!isVisible() || !isEnabled()) // отключение физики если компонент недоступен
                    return;

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
        };



        if (!nodes.isEmpty()) {
            physicThread = new Thread(physicThreadRun, "PhysicThread");

            initializeNodes();

            world.setGravity(GRAVITY);
            world.addStepListener(this);
            physicThread.start();
        }
    }

    private void initializeNodes() {
        nodes.forEach(node -> {
            var body = node.getBody();
            var random = new Random();

            body.translate(random.nextInt(250), random.nextInt(250));
            body.addFixture(Geometry.createCircle((double) NODE_SIZE / 2));
            body.setMass(NODE_MASS);

            body.setLinearDamping(DAMPING);
            body.setAngularDamping(DAMPING);

            world.addBody(body);

            node.getLinks().forEach(link -> {
                var linkBody = link.getBody();

                DistanceJoint<Body> joint = new DistanceJoint<>(body, linkBody, body.getTransform().getTranslation(), linkBody.getTransform().getTranslation());
                joint.setRestDistance(REST_DISTANCE);
                joint.setSpringEnabled(true);
                joint.setSpringDamperEnabled(true);
                joint.setSpringFrequency(SPRING_FREQUENCY);
                world.addJoint(joint);
            });
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

    private Node getFocusatedNode() {
        for (Node node : nodes) {
            if (node.getBody().getWorldCenter().distance(getMouseWorldPosition()) <= NODE_SIZE) {
                return node;
            }
        }

        return null;
    }

    // функция для обновления графа
    public void restart() {
        physicThreadLive = false;

        try {
            physicThread.join();
        } catch (InterruptedException ignored) {
        }

        nodes.clear();
        initializeNodes();

        physicThread = new Thread(physicThreadRun, "PhysicThread");
        physicThread.start();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // TODO: убийца ФПС
        g2d.translate((int) offset.x, (int) offset.y);
        g2d.scale(scale, scale);

        nodes.forEach(node -> {
            int x = (int) node.getBody().getWorldCenter().x;
            int y = (int) node.getBody().getWorldCenter().y;

            g2d.fillOval(x, y, NODE_SIZE, NODE_SIZE);

            g2d.drawString(node.getName(), x, y);
            g2d.drawString(node.getAuthor(), x, (int) (y + NODE_SIZE * 1.5));

            var nodeOffset = NODE_SIZE / 2;

            node.getLinks().forEach(link -> {
                var linkX = (int) link.getBody().getWorldCenter().x;
                var linkY = (int) link.getBody().getWorldCenter().y;

                g2d.drawLine(x + nodeOffset, y + nodeOffset, linkX + nodeOffset, linkY + nodeOffset);
            });
        });

        g2d.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        var focusNode = getFocusatedNode();

        if (focusNode == null)
            return;

        fireActionPerformed(new ActionEvent(focusNode, 0, "node_clicked"));
    }

    // перемещение области
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MOVE_GRAPH) {
            lastMousePos = e.getPoint();
        } else if (e.getButton() == MOVE_NODE) {
            var focusNode = getFocusatedNode();

            if (focusNode == null)
                return;

            draggedNode = focusNode;
            draggedNode.getBody().setMass(MassType.INFINITE);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MOVE_GRAPH) {
            lastMousePos = null;
        } else if (e.getButton() == MOVE_NODE) {
            if (draggedNode != null) {
                draggedNode.getBody().setMass(NODE_MASS);
                draggedNode = null;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastMousePos != null) {
            offset.x += (e.getX() - lastMousePos.x);
            offset.y += (e.getY() - lastMousePos.y);
            lastMousePos = e.getPoint();
            
            repaint();
        }
        
        // перемещение ноды
        if (draggedNode != null) {
            Vector2 targetPos = getMouseWorldPosition();
            
            if (targetPos == null)
                return;
            
            Vector2 nodePos = draggedNode.getBody().getWorldCenter();
            Vector2 force = targetPos.subtract(nodePos);
            
            draggedNode.getBody().translate(force);
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
        
        repaint();
    }

    @Override
    public void end(TimeStep step, PhysicsWorld world) {
		SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public void mouseMoved(MouseEvent e) {}
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

    // возвращает позицию мыши в мировом представлении
    public Vector2 getMouseWorldPosition() {
        var mousePosition = getMousePosition();
        
        if (mousePosition == null)
            return null;

        return Util.pointToVector(mousePosition).subtract(offset).divide(scale);
    }

    public Vector2 getOffset() {
        return offset;
    }

    public double getScale() {
        return scale;
    }
}
