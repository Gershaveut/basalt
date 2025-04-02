package dev.code_offline.basalt.view.tool_panel;

import dev.code_offline.basalt.core.Icon;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ToolPanel extends JPanel {
    private final int TOOL_BUTTON_SIZE = 50;

    public ToolPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        var project = new JButton(Icon.FOLDER.getIcon());
        var graph = new JButton(Icon.GRAPH.getIcon());

        var toolList = new ArrayList<JComponent>();

        toolList.add(project);
        toolList.add(graph);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;

        toolList.forEach(tool -> {
            tool.setPreferredSize(new Dimension(TOOL_BUTTON_SIZE, TOOL_BUTTON_SIZE));
            this.add(tool, c);

            c.gridy++;
            c.weighty++;
        });
    }
}
