package dev.code_offline.basalt.view.frame;

import dev.code_offline.basalt.view.component.GraphPanel;
import dev.code_offline.basalt.view.component.graph.Node;
import dev.code_offline.basalt.view.component.graph.NodeElement;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
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

        this.setVisible(true);
    }
}
