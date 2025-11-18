package dev.code_offline.basalt.view;

import com.javadocking.DockingManager;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.model.FloatDockModel;
import dev.code_offline.basalt.controller.DatabaseController;
import dev.code_offline.basalt.controller.SettingsController;
import dev.code_offline.basalt.controller.StartController;
import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.view.menubar.MenuBar;
import dev.code_offline.basalt.view.start.StartFrame;
import dev.code_offline.basalt.view.tool.AbstractTool;
import dev.code_offline.basalt.view.tool.LogTool;
import dev.code_offline.basalt.view.tool.folder.FolderTool;
import dev.code_offline.basalt.view.tool.graph.GraphTool;
import dev.code_offline.basalt.view.tool.person.PersonsTool;
import dev.code_offline.basalt_share.Util;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationFrame extends JFrame {
    public final Database database;
    
    private final EventListenerList listeners = new EventListenerList();
    
    private boolean debug;

    public ApplicationFrame(Database database, StartFrame startFrame, StartController startController) throws HeadlessException {
        this.database = database;

        this.setTitle(Util.APPLICATION_NAME);
        this.setLayout(new BorderLayout());
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.toFront();
        
        var menuBar = new MenuBar();

        var graph = new Graph();
        var graphTool = new GraphTool(graph, this);
        var folderTool = new FolderTool(this);
        var personsTool = new PersonsTool(this);
        var logTool = new LogTool();
        
        List<AbstractTool> abstractTools = new ArrayList<>();

        // создание модели
        var dockModel = new FloatDockModel();
        dockModel.addOwner("main_frame", this);
        DockingManager.setDockModel(dockModel);

        // добавление в список инструментов
        abstractTools.add(folderTool);
        abstractTools.add(graphTool);
        abstractTools.add(personsTool);
        abstractTools.add(logTool);

        var toolPanel = new ToolPanel(abstractTools);

        // создание табов
        var rightTabDock = new TabDock();
        var leftTabDock = new TabDock();

        // добавление инструментов в табы
        rightTabDock.addDockable(graphTool.getDockable(), new Position());
        rightTabDock.addDockable(personsTool.getDockable(), new Position());
        rightTabDock.addDockable(logTool.getDockable(), new Position());
        leftTabDock.addDockable(folderTool.getDockable(), new Position());
        
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
        
        new DatabaseController(this, graphTool, folderTool, rightTabDock, rightSplitDock, database, menuBar, startFrame, startController, personsTool);
        new SettingsController(menuBar.getSettingsFrame(), this, graphTool.graphCanvas, database);

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
