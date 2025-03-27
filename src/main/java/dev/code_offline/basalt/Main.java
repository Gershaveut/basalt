package dev.code_offline.basalt;

import dev.code_offline.basalt.view.Graph;
import dev.code_offline.basalt.model.NodeElement;
import dev.code_offline.basalt.view.MainFrame;

import javax.swing.*;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		var nodeParent = new NodeElement("Test", "Gershaveut", null);

		var graph = new Graph(
				List.of(nodeParent,
						new NodeElement("Test", "Gershaveut", nodeParent),
						new NodeElement("Test", "Gershaveut", nodeParent),
						new NodeElement("Test", "Gershaveut", null)
				)
		);
		SwingUtilities.invokeLater(() -> new MainFrame(graph));
	}
}
