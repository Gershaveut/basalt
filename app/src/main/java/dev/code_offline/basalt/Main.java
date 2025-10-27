package dev.code_offline.basalt;

import dev.code_offline.basalt.view.start.StartFrame;

import javax.swing.*;
import java.util.logging.Logger;

public class Main {
	public static final byte NETWORK_VERSION = 1;
	
	public static final Logger LOGGER = Logger.getGlobal();

	public static void main(String[] args) {
		SwingUtilities.invokeLater(StartFrame::new);
	}
}
