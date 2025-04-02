package dev.code_offline.basalt.view;

import dev.code_offline.basalt.controller.GraphController;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.view.graph.GraphPanel;
import dev.code_offline.basalt.view.tool_panel.ToolPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
        this.setLayout(new BorderLayout());
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var graph = new Graph();
        graph.initializeSampleData();

        var graphPanel = new GraphPanel(graph);
        this.add(graphPanel);
        var toolPanel = new ToolPanel();
        this.add(toolPanel, BorderLayout.WEST);

        new GraphController(graph, graphPanel);

        this.setVisible(true);
    }
}
