package dev.code_offline.basalt.view.tool.graph;

import dev.code_offline.basalt.core.Util;
import dev.code_offline.basalt.model.graph.Graph;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GraphCanvas extends JPanel {
    private final double NANO_TO_BASE = 1.0e9;

    public final int NODE_SIZE = 25;

    // настройки физики
    public final Vector2 GRAVITY = new Vector2();
    public final MassType NODE_MASS = MassType.NORMAL;
    public final double DAMPING = 0.5;
    public final double REST_DISTANCE = 150;
    public final double SPRING_FREQUENCY = 8;

    private final World<Body> world = new World<>();

    private final Graph graph;
    private final Vector2 offset = new Vector2();

    private double scale = 1.0;

    private boolean physicThreadLive; // нужно что-бы дать знать когда потоку на покой
    private final Runnable physicThreadRun;
    private Thread physicThread;
    private long last;

    public GraphCanvas(Graph graph) {
        this.graph = graph;

        physicThreadRun = () -> {
            physicThreadLive = true;

            while (physicThreadLive) { // если false поток завершит работу
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

        if (!graph.getNodes().isEmpty()) {
            physicThread = new Thread(physicThreadRun, "PhysicThread");

            initializeNodes();

            world.setGravity(GRAVITY);
            physicThread.start();
        }
    }

    public Vector2 getOffset() {
        return offset;
    }

    public void setOffset(double x, double y) {
        offset.x = x;
        offset.y = y;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public World<Body> getWorld() {
        return world;
    }

    private void initializeNodes() {
        graph.getNodes().forEach(node -> {
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


    // функция для обновления графа
    public void restart() {
        physicThreadLive = false;

        try {
            physicThread.join();
        } catch (InterruptedException ignored) {
        }

        graph.getNodes().clear();
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

        graph.getNodes().forEach(node -> {
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


    // возвращает позицию мыши в мировом представлении
    public Vector2 getMouseWorldPosition() {
        var mousePosition = getMousePosition();

        if (mousePosition == null)
            return null;

        return Util.pointToVector(mousePosition).subtract(offset).divide(scale);
    }
}
