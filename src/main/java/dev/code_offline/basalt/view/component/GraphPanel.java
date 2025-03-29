package dev.code_offline.basalt.view.component;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.view.component.graph.GraphCanvas;
import dev.code_offline.basalt.view.component.graph.Node;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

public class GraphPanel extends JPanel {
    private final GraphCanvas graphCanvas;

    public GraphPanel(List<Node> nodes) {
        super(new BorderLayout());

        graphCanvas = new GraphCanvas(nodes);
        this.add(graphCanvas, BorderLayout.CENTER);

        if (Main.DEBUG) {
            var debugPanel = new JPanel();

            var offsetLabel = new JLabel();
            var scaleLocationLabel = new JLabel();
            var mouseLocationLabel = new JLabel();
            var firstNodeLocationLabel = new JLabel();

            debugPanel.add(offsetLabel);
            debugPanel.add(scaleLocationLabel);
            debugPanel.add(mouseLocationLabel);
            debugPanel.add(firstNodeLocationLabel);

            debugPanel.setLayout(new GridLayout(0, 1));
            debugPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
            this.add(debugPanel, BorderLayout.SOUTH);

            var mouseAdapter = new MouseAdapter() {
                void update(MouseEvent e) {
                    offsetLabel.setText("Graph offset: " + (int) graphCanvas.getOffset().x + " " +  (int) graphCanvas.getOffset().y);
                    scaleLocationLabel.setText("Graph scale: " + graphCanvas.getScale());

                    mouseLocationLabel.setText("Mouse location: " + graphCanvas.getMouseWorldPosition());
                    firstNodeLocationLabel.setText("First node location: " + graphCanvas.nodes.getFirst().getBody().getWorldCenter());
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    update(e);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    update(e);
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    update(e);
                }
            };

            graphCanvas.addMouseMotionListener(mouseAdapter);
            graphCanvas.addMouseWheelListener(mouseAdapter);
        }
    }
}
