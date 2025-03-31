package dev.code_offline.basalt.view;

import dev.code_offline.basalt.controller.GraphController;
import dev.code_offline.basalt.core.Icon;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.view.graph.GraphPanel;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.model.graph.NodeElement;
import dev.code_offline.basalt.view.tool_panel.ToolPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static dev.code_offline.basalt.core.Icon.FOLDER;
import static dev.code_offline.basalt.core.Icon.GRAPH;

public class MainFrame extends JFrame {
    private GraphController graphController;

    private final GridBagLayout layout = new GridBagLayout();

    public MainFrame() throws HeadlessException {
        this.setLayout(new BorderLayout());
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var graph = new Graph();
        graph.initializeSampleData();

        var graphPanel = new GraphPanel(graph);
        graphController = new GraphController(graph, graphPanel);

        this.add(graphPanel);
        this.add(new ToolPanel(), BorderLayout.WEST);

        this.setVisible(true);
    }
}
