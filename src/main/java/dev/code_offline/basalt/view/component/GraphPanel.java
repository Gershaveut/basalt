package dev.code_offline.basalt.view.component;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.view.component.graph.GraphCanvas;
import dev.code_offline.basalt.view.component.graph.Node;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class GraphPanel extends JPanel {
    private final GraphCanvas graphCanvas;

    public GraphPanel(List<Node> nodes) {
        this.setLayout(new BorderLayout());

        graphCanvas = new GraphCanvas(nodes);

        this.add(graphCanvas, BorderLayout.CENTER);

        if (Main.DEBUG) {
            var debugPanel = new JPanel();

            var mouseLocationLabel = new JLabel();

            debugPanel.add(mouseLocationLabel);

            debugPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            debugPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
            this.add(debugPanel, BorderLayout.NORTH);

            graphCanvas.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mouseLocationLabel.setText(e.getPoint().toString());
                }
            });
        }
    }
}
