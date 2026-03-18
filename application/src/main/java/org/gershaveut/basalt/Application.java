package org.gershaveut.basalt;

import org.gershaveut.basalt.view.start.StartFrame;

import javax.swing.*;

public class Application {
	static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new StartFrame(args));
	}
}
