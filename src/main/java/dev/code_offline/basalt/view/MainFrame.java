package dev.code_offline.basalt.view;

import com.javadocking.DockingManager;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockingMode;
import com.javadocking.model.FloatDockModel;
import dev.code_offline.basalt.controller.GraphController;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.view.graph.GraphPanel;
import dev.code_offline.basalt.view.markdown.MarkdownEditor;
import dev.code_offline.basalt.view.tool.FolderPanel;
import dev.code_offline.basalt.view.tool.ToolPanel;

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
        var toolPanel = new ToolPanel();
        var folderPanel = new FolderPanel();

        new GraphController(graph, graphPanel);

        FloatDockModel dockModel = new FloatDockModel();
        dockModel.addOwner("frame0", this);

        DockingManager.setDockModel(dockModel);

        Dockable graphDock = new DefaultDockable("Window1", graphPanel, "Граф", null, DockingMode.ALL);
        Dockable folderDock = new DefaultDockable("Window2", folderPanel, "Проект", null, DockingMode.ALL);

        TabDock rightTabDock = new TabDock();
        TabDock leftTabDock = new TabDock();

        rightTabDock.addDockable(graphDock, new Position(0));
        leftTabDock.addDockable(folderDock, new Position(0));

        SplitDock rightSplitDock = new SplitDock();
        SplitDock leftSplitDock = new SplitDock();

        rightSplitDock.addChildDock(rightTabDock, new Position(Position.CENTER));
        leftSplitDock.addChildDock(leftTabDock, new Position(Position.CENTER));

        dockModel.addRootDock("rightdock", rightSplitDock, this);
        dockModel.addRootDock("leftdock", leftSplitDock, this);

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        rightSplitPane.setDividerSize(0);
        splitPane.setDividerLocation(150);

        rightSplitPane.add(rightSplitDock);
        splitPane.setRightComponent(rightSplitPane);
        splitPane.setLeftComponent(leftSplitDock);

        add(splitPane, BorderLayout.CENTER);
        add(toolPanel, BorderLayout.WEST);

        add(new MarkdownEditor());

        this.setVisible(true);
    }
}
