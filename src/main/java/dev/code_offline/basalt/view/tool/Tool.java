package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;

import javax.swing.*;

public final class Tool extends StateActionDockable implements BasaltDockable {
    private final BasaltDockable basaltDockable;

    public Tool(BasaltDockable basaltDockable) {
        super(new DefaultDockable(basaltDockable.getID(), basaltDockable.getContent(), basaltDockable.getTitle(), basaltDockable.getIcon(), basaltDockable.getDockingModes()),
                new DefaultDockableStateActionFactory(), DockableState.statesClosed());

        this.basaltDockable = basaltDockable;
    }

    @Override
    public ImageIcon getIconOriginal() {
        return basaltDockable.getIconOriginal();
    }
}
