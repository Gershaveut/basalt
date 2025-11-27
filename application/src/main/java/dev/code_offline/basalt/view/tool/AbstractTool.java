package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;
import org.springframework.lang.Nullable;

import javax.swing.*;

public abstract class AbstractTool extends JPanel {
    public static final int ICON_SIZE = 15;
    
    private Dockable dockable;
    private DefaultDockable delegate;
    
    private void ensure() {
        if (delegate == null) {
            delegate = new DefaultDockable(getID(), this, getTitle(), getIcon(), getDockingModes());
            dockable = new StateActionDockable(delegate, new DefaultDockableStateActionFactory(), DockableState.statesClosed());
        }
    }
    
    protected abstract String getID();
    protected abstract String getTitle();
    protected abstract int getDockingModes();
    @Nullable
    public abstract ImageIcon getIconOriginal();
    
    private @Nullable Icon getIcon() {
        if (getIconOriginal() != null)
            return new ImageIcon(getIconOriginal().getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, 0));
        else
            return null;
    }
    
    public Dockable getDockable() {
        ensure();
        return dockable;
    }
    
    protected DefaultDockable getDelegate() {
        ensure();
        return delegate;
    }
}