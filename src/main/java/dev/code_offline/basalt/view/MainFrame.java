package dev.code_offline.basalt.view;

import com.javadocking.DockingManager;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.model.FloatDockModel;
import dev.code_offline.basalt.controller.GraphController;
import dev.code_offline.basalt.controller.NoteController;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.view.menubar.MenuBar;
import dev.code_offline.basalt.view.tool.FolderPanel;
import dev.code_offline.basalt.view.tool.MarkdownEditorPanel;
import dev.code_offline.basalt.view.tool.Tool;
import dev.code_offline.basalt.view.tool.graph.GraphPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
        this.setLayout(new BorderLayout());
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var menuBar = new MenuBar();

        var graph = new Graph();
        var graphPanel = new GraphPanel(graph);

        var folderPanel = new FolderPanel();
        var markdownEditorPanel = new MarkdownEditorPanel();

        List<Tool> tools = new ArrayList<>();

        new GraphController(graph, graphPanel);
        new NoteController(graphPanel, markdownEditorPanel, folderPanel);

        // создание модели
        FloatDockModel dockModel = new FloatDockModel();
        dockModel.addOwner("main_frame", this);

        DockingManager.setDockModel(dockModel);

        // создание инструментов
        var graphDock = new Tool(graphPanel);
        var folderDock = new Tool(folderPanel);
        var markdownEditorDock = new Tool(markdownEditorPanel);

        // добавление в список инструментов
        tools.add(graphDock);
        tools.add(folderDock);
        tools.add(markdownEditorDock);

        var toolPanel = new ToolPanel(tools);

        // создание табов
        var rightTabDock = new TabDock();
        var leftTabDock = new TabDock();

        // добавление инструментов в табы
        rightTabDock.addDockable(graphDock, new Position(0));
        leftTabDock.addDockable(folderDock, new Position(0));

        // создание доков
        var rightSplitDock = new SplitDock();
        var leftSplitDock = new SplitDock();

        // добавление табов в доки
        rightSplitDock.addChildDock(rightTabDock, new Position(Position.CENTER));
        leftSplitDock.addChildDock(leftTabDock, new Position(Position.CENTER));

        // добавление доков в модель
        dockModel.addRootDock("right_dock", rightSplitDock, this);
        dockModel.addRootDock("left_dock", leftSplitDock, this);

        // создание сплит панелей
        var rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // настройка разделителя
        rightSplitPane.setDividerSize(0);
        splitPane.setDividerLocation(150);

        // добавление доков в сплит панели
        rightSplitPane.add(rightSplitDock);
        splitPane.setRightComponent(rightSplitPane);
        splitPane.setLeftComponent(leftSplitDock);

        // добавление компонентов
        add(splitPane, BorderLayout.CENTER);
        add(toolPanel, BorderLayout.WEST);

        this.setJMenuBar(menuBar);
        this.setVisible(true);
    }
}
