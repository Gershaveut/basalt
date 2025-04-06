package dev.code_offline.basalt.controller;

import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.view.tool.graph.GraphCanvas;
import dev.code_offline.basalt.view.tool.graph.GraphPanel;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.listener.StepListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class GraphController implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, StepListener<Body> {
    private final int MOVE_GRAPH = MouseEvent.BUTTON3;
    private final int MOVE_NODE = MouseEvent.BUTTON1;

    private final double SCALE_MAX = 5;
    private final double SCALE_MIN = 0.3;
    private final double SCALE_POW = 1.1;

    private @Nullable Point lastMousePos;
    private @Nullable Node draggedNode;

    private final Graph graph;
    private final GraphPanel graphPanel;
    private final GraphCanvas graphCanvas;

    public GraphController(Graph graph, GraphPanel graphPanel) {
        this.graph = graph;
        this.graphPanel = graphPanel;
        this.graphCanvas = graphPanel.graphCanvas;

        setupListeners();
        centerGraph();
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

            var x = ((graphPanel.graphCanvas.getWidth() / graphPanel.graphCanvas.getScale() - graphWidth) / 2 - minX);
            var y = ((graphPanel.graphCanvas.getHeight() / graphPanel.graphCanvas.getScale() - graphHeight) / 2 - minY);

            graphPanel.graphCanvas.setOffset(x, y);
        }
    }

    public void setupListeners() {
        graphCanvas.addMouseListener(this);
        graphCanvas.addMouseMotionListener(this);
        graphCanvas.addMouseWheelListener(this);
        graphCanvas.addComponentListener(this);
        graphCanvas.getWorld().addStepListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MOVE_GRAPH) {
            lastMousePos = e.getPoint();
        } else if (e.getButton() == MOVE_NODE) {
            var focusNode = graphCanvas.getFocusatedNode();

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
                draggedNode.getBody().setMass(graphCanvas.NODE_MASS);
                draggedNode = null;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastMousePos != null) {
            var newOffsetX = graphCanvas.getOffset().x + (e.getX() - lastMousePos.x);
            var newOffsetY = graphCanvas.getOffset().y + (e.getY() - lastMousePos.y);
            graphCanvas.setOffset(newOffsetX, newOffsetY);
            lastMousePos = e.getPoint();

            graphCanvas.repaint();
        }

        // перемещение ноды
        if (draggedNode != null) {
            var targetPos = graphCanvas.getMouseWorldPosition();

            if (targetPos == null)
                return;

            Vector2 nodePos = draggedNode.getBody().getWorldCenter();
            Vector2 force = targetPos.subtract(nodePos);

            draggedNode.getBody().translate(force);
            graphCanvas.repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        var offset = graphCanvas.getOffset();
        var scale = graphCanvas.getScale();

        double mouseX = e.getX();
        double mouseY = e.getY();

        double graphMouseX = (mouseX - offset.x) / scale;
        double graphMouseY = (mouseY - offset.y) / scale;

        double wheelDelta = e.getPreciseWheelRotation();
        double scaleFactor = Math.pow(SCALE_POW, -wheelDelta);

        graphCanvas.setScale(scale *= scaleFactor);
        graphCanvas.setScale(Math.max(SCALE_MIN, Math.min(SCALE_MAX, scale)));

        if (SCALE_MAX > scale && SCALE_MIN < scale) {
            offset.x = mouseX - graphMouseX * scale;
            offset.y = mouseY - graphMouseY * scale;
        }

        graphCanvas.repaint();
    }

    @Override
    public void end(TimeStep step, PhysicsWorld<Body, ?> world) {
        SwingUtilities.invokeLater(graphCanvas::repaint);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (graphPanel.graphCanvas.getWidth() > 0 && graphPanel.graphCanvas.getHeight() > 0) {
            centerGraph();
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void begin(TimeStep step, PhysicsWorld<Body, ?> world) {}
    @Override public void updatePerformed(TimeStep step, PhysicsWorld<Body, ?> world) {}
    @Override public void postSolve(TimeStep step, PhysicsWorld<Body, ?> world) {}
    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}
}
