package dev.code_offline.basalt;

import dev.code_offline.basalt.view.MainFrame;

import javax.swing.*;

public class Main {
	public static final boolean DEBUG = true;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainFrame::new);
	}
}
