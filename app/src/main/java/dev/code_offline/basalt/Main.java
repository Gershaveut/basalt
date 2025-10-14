package dev.code_offline.basalt;

import dev.code_offline.basalt.view.StartFrame;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.util.logging.Logger;

public class Main {
	public static Logger logger = Logger.getGlobal();

	public static void main(String[] args) {
		SwingUtilities.invokeLater(StartFrame::new);
	}
}
