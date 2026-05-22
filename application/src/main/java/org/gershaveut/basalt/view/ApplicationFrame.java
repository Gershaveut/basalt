package org.gershaveut.basalt.view;

import com.javadocking.DockingManager;
import com.javadocking.dock.Position;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import com.javadocking.model.FloatDockModel;
import org.apache.commons.text.WordUtils;
import org.gershaveut.basalt.controller.DatabaseController;
import org.gershaveut.basalt.controller.StartController;
import org.gershaveut.basalt.model.database.Database;
import org.gershaveut.basalt.model.graph.Graph;
import org.gershaveut.basalt.view.menubar.MenuBar;
import org.gershaveut.basalt.view.start.StartFrame;
import org.gershaveut.basalt.view.tool.AbstractTool;
import org.gershaveut.basalt.view.tool.LogTool;
import org.gershaveut.basalt.view.tool.file.FilesTool;
import org.gershaveut.basalt.view.tool.graph.GraphTool;
import org.gershaveut.basalt.view.tool.person.PersonsTool;
import org.gershaveut.basalt_share.Util;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ApplicationFrame extends JFrame {
    public final Database database;
    
    private final EventListenerList listeners = new EventListenerList();
    
    private boolean debug;

    public ApplicationFrame(Database database, StartFrame startFrame, StartController startController) throws HeadlessException {
        this.database = database;

        this.setTitle(WordUtils.capitalize(Util.APPLICATION_NAME));
        this.setLayout(new BorderLayout());
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.toFront();
        
        var menuBar = new MenuBar(startFrame.settingsFrame);

        var graph = new Graph();
        var graphTool = new GraphTool(graph, this);
        var folderTool = new FilesTool(this);
        var personsTool = new PersonsTool(this);
        var logTool = new LogTool();
        
        List<AbstractTool> abstractTools = new ArrayList<>();

        DockingManager.setComponentFactory(new ApplicationSwComponentFactory());

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
        startFrame.settingsController.loadApplicationSettings(this, graphTool.graphCanvas, database);

        // закрытие лишних инструментов
        var toClose = new ArrayList<>(abstractTools);

        toClose.remove(graphTool);
        toClose.remove(folderTool);

        toClose.forEach(abstractTool -> {
            var closeAction = new DefaultDockableStateAction(abstractTool.getDockable(), DockableState.CLOSED);

            closeAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Close"));
        });
        
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
