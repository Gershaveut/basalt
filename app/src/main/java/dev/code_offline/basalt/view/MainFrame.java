package dev.code_offline.basalt.view;

import com.javadocking.DockingManager;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.model.FloatDockModel;
import dev.code_offline.basalt.controller.GraphController;
import dev.code_offline.basalt.controller.LogController;
import dev.code_offline.basalt.controller.NoteController;
import dev.code_offline.basalt.controller.SettingsController;
import dev.code_offline.basalt.core.client.Client;
import dev.code_offline.basalt.core.client.JSONClient;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.view.menubar.MenuBar;
import dev.code_offline.basalt.view.tool.LogPanel;
import dev.code_offline.basalt.view.tool.Tool;
import dev.code_offline.basalt.view.tool.folder.FolderPanel;
import dev.code_offline.basalt.view.tool.graph.GraphPanel;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    public final Client client;
    
    private final EventListenerList listeners = new EventListenerList();
    
    private boolean debug;

    public MainFrame(Client client) throws HeadlessException {
        this.client = client;

        this.setTitle("Basalt");
        this.setLayout(new BorderLayout());
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var menuBar = new MenuBar();

        var graph = new Graph();
        var graphPanel = new GraphPanel(graph, this, client.isOffline());
        var folderPanel = new FolderPanel();
        var logPanel = new LogPanel();

        List<Tool> tools = new ArrayList<>();

        // создание модели
        var dockModel = new FloatDockModel();
        dockModel.addOwner("main_frame", this);
        DockingManager.setDockModel(dockModel);

        // создание инструментов
        var graphDock = new Tool(graphPanel);
        var folderDock = new Tool(folderPanel);
        var logDock = new Tool(logPanel);
        
        // добавление в список инструментов
        tools.add(folderDock);
        tools.add(graphDock);
        tools.add(logDock);

        var toolPanel = new ToolPanel(tools);

        // создание табов
        var rightTabDock = new TabDock();
        var leftTabDock = new TabDock();

        // добавление инструментов в табы
        rightTabDock.addDockable(graphDock, new Position());
        leftTabDock.addDockable(folderDock, new Position());
        rightTabDock.addDockable(logDock, new Position());

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
        
        new LogController(logPanel);
        
        new GraphController(graph, graphPanel);
        new NoteController(this, graphPanel, folderPanel, rightTabDock, rightSplitDock, client, menuBar);
        new SettingsController(menuBar.getSettingsFrame(), this);

        this.setJMenuBar(menuBar);
        this.setVisible(true);
    }

    public void addDebugModeListener(DebugModeListener debugModeListener) {
        listeners.add(DebugModeListener.class, debugModeListener);
        
        if (debug)
            debugModeListener.debugEnabled();
    }

    public void removeDebugModeListener(DebugModeListener debugModeListener) {
        listeners.remove(DebugModeListener.class, debugModeListener);
    }

    public void enableDebug() {
        if (!debug) {
            for (DebugModeListener listener : listeners.getListeners(DebugModeListener.class)) {
                listener.debugEnabled();
            }
            
            debug = true;
        }
    }
}
