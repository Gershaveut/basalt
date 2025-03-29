package dev.code_offline.basalt;

import dev.code_offline.basalt.view.component.graph.GraphCanvas;
import dev.code_offline.basalt.view.component.graph.NodeElement;
import dev.code_offline.basalt.view.frame.MainFrame;

import javax.swing.*;
import java.util.List;

public class Main {
	public static boolean DEBUG = true;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainFrame::new);
	}
}
