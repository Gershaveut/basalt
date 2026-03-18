package org.gershaveut.basalt.view;

import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import org.gershaveut.basalt.view.tool.AbstractTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

public class ToolPanel extends JPanel {
    private static final int TOOL_BUTTON_SIZE = 50;
    private static final int ICON_SIZE = (int) (TOOL_BUTTON_SIZE * 0.65);

    public ToolPanel(List<AbstractTool> abstractTools) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        abstractTools.forEach(abstractTool -> {
            var toolButton = new JButton(new ImageIcon(Objects.requireNonNull(abstractTool.getIconOriginal()).getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, 0)));

            var dockable = abstractTool.getDockable();
            
            var closeAction = new DefaultDockableStateAction(dockable, DockableState.CLOSED);
            var restoreAction = new DefaultDockableStateAction(dockable, DockableState.NORMAL);

            toolButton.addActionListener(e -> {
                if (dockable.getState() != DockableState.CLOSED)
                {
                    closeAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Close"));
                }
                else
                {
                    restoreAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Restore"));
                }
            });

            toolButton.setPreferredSize(new Dimension(TOOL_BUTTON_SIZE, TOOL_BUTTON_SIZE));
            this.add(toolButton);
        });
    }
}
