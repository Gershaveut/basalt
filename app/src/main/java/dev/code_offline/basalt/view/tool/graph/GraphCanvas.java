package dev.code_offline.basalt.view.tool.graph;

import dev.code_offline.basalt.Util;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.graph.Node;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.DistanceJoint;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("BusyWait")
public class GraphCanvas extends JComponent implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphCanvas.class);
    
    private final double NANO_TO_BASE = 1.0e9;

    public final int NODE_SIZE = 25;

    // настройки физики
    public final Vector2 GRAVITY = new Vector2();
    public final MassType NODE_MASS = MassType.NORMAL;
    public final double DAMPING = 0.5;
    public final double REST_DISTANCE = 150;
    public final double SPRING_FREQUENCY = 8;
    
    private final int MOVE_GRAPH = MouseEvent.BUTTON3;
    private final int MOVE_NODE = MouseEvent.BUTTON1;
    
    private final double SCALE_MAX = 5;
    private final double SCALE_MIN = 0.3;
    private final double SCALE_POW = 1.1;
    
    public boolean debug;

    private final World<Body> world = new World<>();

    private Graph graph;
    
    private final Vector2 offset = new Vector2();

    private double scale = 1.0;

    private boolean physicThreadLive; // нужно что-бы дать знать когда потоку на покой
    private final Runnable physicThreadRun;
    private @Nullable Thread physicThread;
    private long last;
    
    private @Nullable Point lastMousePos;
    private @Nullable Node draggedNode;
    
    private boolean paintThreadLive; // нужно что-бы дать знать когда потоку на покой
    private final Runnable paintThreadRun;
    private @Nullable Thread paintThread;
    
    private int maxFps = 60;
    private int physicMaxFps = 120;
    
    private int fps;
    private int physicFps;
    
    private int countFrame;
    private int countPhysicFrame;
    private long lastFrame;
    private long lastPhysicFrame;

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
               
                SwingUtilities.invokeLater(() -> {
                    // перемещение ноды
                    if (draggedNode != null) {
                        var targetPos = this.getMouseWorldPosition();
                        
                        if (targetPos == null)
                            return;
                        else
                            targetPos = targetPos.subtract(new Vector2((double) NODE_SIZE / 2, (double) NODE_SIZE / 2));
                        
                        Vector2 nodePos = draggedNode.getBody().getWorldCenter();
                        Vector2 force = targetPos.subtract(nodePos);
                        
                        draggedNode.getBody().translate(force);
                    }
                });
                
                ++countPhysicFrame;
                
                if (time - lastPhysicFrame > NANO_TO_BASE) {
                    physicFps = countPhysicFrame;
                    
                    countPhysicFrame = 0;
                    lastPhysicFrame = time;
                }
                
                try {
                    // освобождение процессора
                    Thread.sleep(1000 / physicMaxFps);
                } catch (InterruptedException exception) {
					LOGGER.error("Physic thread error", exception);
                }
            }
        };
        
        world.setGravity(GRAVITY);

        paintThreadRun = () -> {
            paintThreadLive = true;
            
            while (paintThreadLive) {
                this.repaint();
                
                var time = System.nanoTime();
                
                ++countFrame;
                
                if (time - lastFrame > NANO_TO_BASE) {
                    fps = countFrame;
                    
                    countFrame = 0;
                    lastFrame = time;
                }
                
                try {
                    Thread.sleep(1000 / maxFps);
                } catch (InterruptedException exception) {
					LOGGER.error("Paint thread error", exception);
                }
            }
        };
        
        if (!graph.getNodes().isEmpty())
            initializeNodes();
        
        initializeThreads();
        
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addComponentListener(this);
    }
    
    private void initializeThreads() {
        physicThread = new Thread(physicThreadRun, "PhysicThread");
        paintThread = new Thread(paintThreadRun, "PaintThread");
        
        var handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                restart();
            }
        };
        
        physicThread.setUncaughtExceptionHandler(handler);
        paintThread.setUncaughtExceptionHandler(handler);
        
        physicThread.start();
        paintThread.start();
    }
    
    public void dispose() {
        if (physicThreadLive) {
            physicThreadLive = false;
                
            try {
                assert physicThread != null;
                physicThread.join();
            } catch (InterruptedException ignored) {
            }
        }
        if (paintThreadLive) {
            paintThreadLive = false;
            
            try {
                assert paintThread != null;
                paintThread.join();
            } catch (InterruptedException ignored) {
            }
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
    
    public int getMaxFps() {
        return maxFps;
    }
    
    public void setMaxFps(int maxFps) {
        this.maxFps = maxFps;
    }
    
    public int getPhysicMaxFps() {
        return physicMaxFps;
    }
    
    public void setPhysicMaxFps(int physicMaxFps) {
        this.physicMaxFps = physicMaxFps;
    }
    
    public int getFps() {
        return fps;
    }
    
    public int getPhysicFps() {
        return physicFps;
    }
    
    private void initializeNodes() {
        graph.getNodes().forEach(node -> {
            var body = node.getBody();
            var random = new Random();

            body.translate(random.nextInt(500), random.nextInt(500));
            body.addFixture(Geometry.createCircle((double) NODE_SIZE / 2));
            body.setMass(NODE_MASS);

            body.setLinearDamping(DAMPING);
            body.setAngularDamping(DAMPING);

            world.addBody(body);
        });
        
        graph.getNodes().forEach(node -> {
            var body = node.getBody();
            
            node.getLinks().forEach(id -> {
                try {
                    var linkBody = graph.getNodes().stream().filter(node1 -> node1.getId() == id).findFirst().orElseThrow().getBody();
                    
                    DistanceJoint<Body> joint = new DistanceJoint<>(body, linkBody, body.getTransform().getTranslation(), linkBody.getTransform().getTranslation());
                    joint.setRestDistance(REST_DISTANCE);
                    joint.setSpringEnabled(true);
                    joint.setSpringDamperEnabled(true);
                    joint.setSpringFrequency(SPRING_FREQUENCY);
                    world.addJoint(joint);
                } catch (Exception ignored) {
                    LOGGER.warn("Note Id: {} Name: {} causes error", node.getId(), node.getName());
                }
            });
        });
    }

    public void restart() {
        dispose();
    
        initializeThreads();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // убийца ФПС
        g2d.translate((int) offset.x, (int) offset.y);
        g2d.scale(scale, scale);

        graph.getNodes().forEach(node -> {
            int x = (int) node.getBody().getWorldCenter().x;
            int y = (int) node.getBody().getWorldCenter().y;

            g2d.fillOval(x, y, NODE_SIZE, NODE_SIZE);

            g2d.drawString(node.getName(), x, y);
            
            g2d.drawString(node.getAuthor(), x, (int) (y + NODE_SIZE * 1.5));
            
            var nodeOffset = NODE_SIZE / 2;
           
            node.getLinks().forEach(id -> {
                try {
                    var link = graph.getNodes().stream().filter(node1 -> node1.getId() == id).findFirst().orElseThrow();
                    
                    var linkX = (int) link.getBody().getWorldCenter().x;
                    var linkY = (int) link.getBody().getWorldCenter().y;
                    
                    g2d.drawLine(x + nodeOffset, y + nodeOffset, linkX + nodeOffset, linkY + nodeOffset);
                } catch (Exception ignored) {
                }
            });
            
            
            if (debug) {
                int debugX = (int) (x + NODE_SIZE * 1.5);

                g2d.drawString("Id: " + node.getId(), debugX, y + 10);
                g2d.drawString("Links: " + node.getLinks(), debugX, y + 20);
            }
        });

        g2d.dispose();
    }

    // возвращает позицию мыши в мировом представлении
    public @Nullable Vector2 getMouseWorldPosition() {
        var mousePosition = getMousePosition();

        if (mousePosition == null)
            return null;

        return Util.pointToVector(mousePosition).subtract(offset).divide(scale);
    }

    public @Nullable Node getFocusatedNode() {
        for (Node node : graph.getNodes()) {
            if (node.getBody().getWorldCenter().distance(getMouseWorldPosition()) <= NODE_SIZE) {
                return node;
            }
        }

        return null;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;

        initializeNodes();
    }
    
    private void centerGraph() {
        if (!graph.getNodes().isEmpty()) {
            var arrayX = graph.getNodes().stream().mapToDouble(n -> n.getBody().getWorldCenter().x).toArray();
            var arrayY = graph.getNodes().stream().mapToDouble(n -> n.getBody().getWorldCenter().y).toArray();
            
            double minX = Arrays.stream(arrayX).min().orElseThrow();
            double maxX = Arrays.stream(arrayX).max().orElseThrow();
            
            double minY = Arrays.stream(arrayY).min().orElseThrow();
            double maxY = Arrays.stream(arrayY).max().orElseThrow();
            
            double graphWidth = maxX - minX;
            double graphHeight = maxY - minY;
            
            var x = ((this.getWidth() / this.getScale() - graphWidth) / 2 - minX);
            var y = ((this.getHeight() / this.getScale() - graphHeight) / 2 - minY);
            
            this.setOffset(x, y);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MOVE_GRAPH) {
            lastMousePos = e.getPoint();
        } else if (e.getButton() == MOVE_NODE) {
            var focusNode = this.getFocusatedNode();
            
            if (focusNode == null)
                return;
            
            draggedNode = focusNode;
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
            var newOffsetX = this.getOffset().x + (e.getX() - lastMousePos.x);
            var newOffsetY = this.getOffset().y + (e.getY() - lastMousePos.y);
            this.setOffset(newOffsetX, newOffsetY);
            lastMousePos = e.getPoint();
        }
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        var offset = this.getOffset();
        var scale = this.getScale();
        
        double mouseX = e.getX();
        double mouseY = e.getY();
        
        double graphMouseX = (mouseX - offset.x) / scale;
        double graphMouseY = (mouseY - offset.y) / scale;
        
        double wheelDelta = e.getPreciseWheelRotation();
        double scaleFactor = Math.pow(SCALE_POW, -wheelDelta);
        
        scale = Math.max(SCALE_MIN, Math.min(SCALE_MAX, scale * scaleFactor));
        
        this.setScale(scale);
        
        if (SCALE_MAX >= scale && SCALE_MIN <= scale) {
            offset.x = mouseX - graphMouseX * scale;
            offset.y = mouseY - graphMouseY * scale;
        }
    }
    
    @Override
    public void componentResized(ComponentEvent e) {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            centerGraph();
        }
    }
    
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}
}
