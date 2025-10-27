package dev.code_offline.basalt.view.tool.graph;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.view.BasaltFrame;
import dev.code_offline.basalt.view.DebugModeListener;
import dev.code_offline.basalt.view.tool.BasaltDockable;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class GraphPanel extends JPanel implements BasaltDockable, DebugModeListener {
    public final GraphCanvas graphCanvas;

    public GraphPanel(Graph graph, BasaltFrame basaltFrame, boolean isOffline) {
        super(new BorderLayout());

        graphCanvas = new GraphCanvas(graph, isOffline);
        this.add(graphCanvas, BorderLayout.CENTER);

        basaltFrame.addDebugModeListener(this);
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

    @Override
    public void debugEnabled() {
        var debugPanel = new JPanel(new GridLayout(0, 1));
        
        var fpsLabel = new JLabel();
        var physicFpsLabel = new JLabel();
        var offsetLabel = new JLabel();
        var scaleLocationLabel = new JLabel();
        var mouseLocationLabel = new JLabel();

        debugPanel.add(fpsLabel);
        debugPanel.add(physicFpsLabel);
        debugPanel.add(new JSeparator());
        debugPanel.add(offsetLabel);
        debugPanel.add(scaleLocationLabel);
        debugPanel.add(mouseLocationLabel);

        debugPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
        this.add(debugPanel, BorderLayout.SOUTH);

        var mouseAdapter = new MouseAdapter() {
            void update(MouseEvent e) {
                fpsLabel.setText("FPS: " + graphCanvas.getFps());
                physicFpsLabel.setText("Physic FPS: " + graphCanvas.getPhysicFps());
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
        
        graphCanvas.debug = true;
    }
}
