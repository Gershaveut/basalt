package dev.code_offline.basalt;

import dev.code_offline.basalt.view.frame.MainFrame;

import javax.swing.*;

public class Main {
	public static boolean DEBUG = true;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainFrame::new);
	}
}
