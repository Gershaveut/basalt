package dev.code_offline.basalt;

import dev.code_offline.basalt.view.start.StartFrame;

import javax.swing.*;

public class Application {
	static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new StartFrame(args));
	}
}
