package dev.code_offline.basalt.view.frame;

import dev.code_offline.basalt.view.component.GraphPanel;
import dev.code_offline.basalt.view.component.graph.Node;
import dev.code_offline.basalt.view.component.graph.NodeElement;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var nodeParent = new NodeElement("Test", "Gershaveut", null);

        List<Node> nodes = List.of(nodeParent,
                new NodeElement("Test", "Gershaveut", nodeParent),
                new NodeElement("Test", "Gershaveut", nodeParent),
                new NodeElement("Test", "Gershaveut", nodeParent)
        );

        this.add(new GraphPanel(nodes));

        this.setVisible(true);
    }
}
