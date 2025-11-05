package dev.code_offline.basalt;

import com.formdev.flatlaf.FlatLightLaf;
import dev.code_offline.basalt.view.start.StartFrame;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		FlatLightLaf.setup();
		
		SwingUtilities.invokeLater(StartFrame::new);
	}
}
