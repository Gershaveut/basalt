package dev.code_offline.basalt.view.frame;

import dev.code_offline.basalt.view.Icon;
import dev.code_offline.basalt.view.component.GraphPanel;
import dev.code_offline.basalt.view.component.graph.Node;
import dev.code_offline.basalt.view.component.graph.NodeElement;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainFrame extends JFrame {
    private final int TOOL_BUTTON_SIZE = 50;

    private final GridBagLayout layout = new GridBagLayout();

    public MainFrame() throws HeadlessException {
        this.setLayout(new BorderLayout());
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
			var random = new Random();
			var linkList = new ArrayList<Node>();

			if (random.nextInt(3) == 1 && nodes.size() > 1)
                linkList.add(nodes.get(random.nextInt(nodes.size() - 1)));

            nodes.add(new NodeElement("Test", "Gershaveut", linkList));
        }

        this.add(new GraphPanel(nodes));

        createToolPanel();

        this.setVisible(true);
    }

    private void createToolPanel() {
        var toolPanel = new JPanel(layout);

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
            toolPanel.add(tool, c);

            c.gridy++;
            c.weighty++;
        });

        this.add(toolPanel, BorderLayout.WEST);
    }
}
