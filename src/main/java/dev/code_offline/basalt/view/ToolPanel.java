package dev.code_offline.basalt.view;

import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.action.DefaultDockableStateAction;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.view.tool.Tool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class ToolPanel extends JPanel {
    private final int TOOL_BUTTON_SIZE = 50;

    public ToolPanel(List<Tool> tools) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;

        tools.forEach(tool -> {
            var toolButton = new JButton(tool.getIconOriginal());

            var closeAction = new DefaultDockableStateAction(tool, DockableState.CLOSED);
            var restoreAction = new DefaultDockableStateAction(tool, DockableState.NORMAL);

            toolButton.addActionListener(e -> {
                if (tool.getState() != DockableState.CLOSED)
                {
                    closeAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Close"));
                }
                else
                {
                    restoreAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Restore"));
                }
            });

            toolButton.setPreferredSize(new Dimension(TOOL_BUTTON_SIZE, TOOL_BUTTON_SIZE));
            this.add(toolButton, c);

            c.gridy++;
            c.weighty++;
        });
    }
}
