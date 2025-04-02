package dev.code_offline.basalt.controller;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.view.graph.GraphPanel;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.listener.StepListenerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class GraphController {
    private final MassType NODE_MASS = MassType.NORMAL;
    private final int NODE_SIZE = 25;

    private final int MOVE_GRAPH = MouseEvent.BUTTON3;
    private final int MOVE_NODE = MouseEvent.BUTTON1;
    private Point lastMousePos;
    private Node draggedNode;

    private final Graph graph;
    private final GraphPanel graphPanel;

    public GraphController(Graph graph, GraphPanel graphPanel) {
        this.graph = graph;
        this.graphPanel = graphPanel;
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
        graphPanel.graphCanvas.addMouseListener(new MouseAdapter() {
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
        });

        graphPanel.graphCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null) {
                    var newOffsetX = graphPanel.graphCanvas.getOffset().x + (e.getX() - lastMousePos.x);
                    var newOffsetY = graphPanel.graphCanvas.getOffset().y + (e.getY() - lastMousePos.y);
                    graphPanel.graphCanvas.setOffset(newOffsetX, newOffsetY);
                    lastMousePos = e.getPoint();

                    graphPanel.graphCanvas.repaint();
                }

                // перемещение ноды
                if (draggedNode != null) {
                    Vector2 targetPos = graphPanel.graphCanvas.getMouseWorldPosition();

                    if (targetPos == null)
                        return;

                    Vector2 nodePos = draggedNode.getBody().getWorldCenter();
                    Vector2 force = targetPos.subtract(nodePos);

                    draggedNode.getBody().translate(force);
                    graphPanel.graphCanvas.repaint();
                }
            }
        });

        graphPanel.graphCanvas.getWorld().addStepListener(new StepListenerAdapter<>() {
                @Override
                public void end(TimeStep step, PhysicsWorld world) {
                    SwingUtilities.invokeLater(graphPanel.graphCanvas::repaint);
                }
            }
        );

        graphPanel.graphCanvas.addComponentListener(
            new ComponentAdapter() {
                // отслеживание изменения размера окна для центрирования графа
                @Override
                public void componentResized(ComponentEvent e) {
                    if (graphPanel.graphCanvas.getWidth() > 0 && graphPanel.graphCanvas.getHeight() > 0) {
                        centerGraph();
                    }
                }
            }
        );
    }


    private Node getFocusatedNode() {
        for (Node node : graph.getNodes()) {
            if (node.getBody().getWorldCenter().distance(graphPanel.graphCanvas.getMouseWorldPosition()) <= NODE_SIZE) {
                return node;
            }
        }

        return null;
    }
}
