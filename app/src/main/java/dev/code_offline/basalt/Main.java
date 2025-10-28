package dev.code_offline.basalt;

import dev.code_offline.basalt.view.start.StartFrame;

import javax.swing.*;

public class Main {
	public static final byte NETWORK_VERSION = 1;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(StartFrame::new);
	}
}
