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
        
        for (int i = 0; i < 999; i++) {
			NodeElement parent = null;
			var random = new Random();
			
			if (random.nextInt(15) == 1)
				parent = (NodeElement) nodes.get(random.nextInt(nodes.size()));
			
            nodes.add(new NodeElement("Test", "Gershaveut", parent));
        }

        this.add(new GraphPanel(nodes));

        this.setVisible(true);
    }
}
