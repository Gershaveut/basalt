package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;
import org.springframework.lang.Nullable;

import javax.swing.*;
import java.awt.*;

public final class Tool extends StateActionDockable implements BasaltDockable {
    private final BasaltDockable basaltDockable;

    public Tool(BasaltDockable basaltDockable) {
        super(new DefaultDockable(basaltDockable.getID(), basaltDockable.getContent(), basaltDockable.getTitle(), basaltDockable.getIcon(), basaltDockable.getDockingModes()),
                new DefaultDockableStateActionFactory(), DockableState.statesClosed());

        this.basaltDockable = basaltDockable;
    }

    public BasaltDockable getBasaltDockable() {
        return basaltDockable;
    }
    
    @Override
    public String getID(){
        return basaltDockable.getID();
    }

    @Override
    public String getTitle() {
        return basaltDockable.getTitle();
    }

    @Override
    public Component getContent() {
        return basaltDockable.getContent();
    }

    @Override
    public int getDockingModes() {
        return basaltDockable.getDockingModes();
    }

    @Override
    public @Nullable ImageIcon getIconOriginal() {
        return basaltDockable.getIconOriginal();
    }

    @Override
    public @Nullable Icon getIcon() {
        return basaltDockable.getIcon();
    }
}
