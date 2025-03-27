package com.gershaveut.jwg;

import com.gershaveut.jwg.graph.Graph;
import com.gershaveut.jwg.graph.NodeElement;

import javax.swing.*;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		var frame = new JFrame();

		var nodeParent = new NodeElement("Test", "Gershaveut", null);

		var graph = new Graph(List.of(nodeParent, new NodeElement("Test", "Gershaveut", nodeParent), new NodeElement("Test", "Gershaveut", nodeParent), new NodeElement("Test", "Gershaveut", null)));
		var scrollPane = new JScrollPane(graph);

		frame.add(scrollPane);
		
		frame.setVisible(true);
	}
}
