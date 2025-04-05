package dev.code_offline.basalt.view.tool.graph;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.view.tool.BasaltDockable;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class GraphPanel extends JPanel implements BasaltDockable {
    public final GraphCanvas graphCanvas;

    public GraphPanel(Graph graph) {
        super(new BorderLayout());

        graphCanvas = new GraphCanvas(graph);
        this.add(graphCanvas, BorderLayout.CENTER);

        if (Main.DEBUG) {
            var debugPanel = new JPanel(new GridLayout(0, 1));

            var offsetLabel = new JLabel();
            var scaleLocationLabel = new JLabel();
            var mouseLocationLabel = new JLabel();

            debugPanel.add(offsetLabel);
            debugPanel.add(scaleLocationLabel);
            debugPanel.add(mouseLocationLabel);

            debugPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
            this.add(debugPanel, BorderLayout.SOUTH);

            var mouseAdapter = new MouseAdapter() {
                void update(MouseEvent e) {
                    offsetLabel.setText("Graph offset: " + (int) graphCanvas.getOffset().x + " " +  (int) graphCanvas.getOffset().y);
                    scaleLocationLabel.setText("Graph scale: " + graphCanvas.getScale());
                    mouseLocationLabel.setText("Mouse location: " + graphCanvas.getMouseWorldPosition());
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

    @Override
    public String getID() {
        return "graph";
    }

    @Override
    public String getTitle() {
        return "Граф";
    }

    @Override
    public Component getContent() {
        return this;
    }

    @Override
    public int getDockingModes() {
        return DockingMode.ALL;
    }

    @Override
    public ImageIcon getIconOriginal() {
        return Icons.GRAPH.getIcon();
    }
}
