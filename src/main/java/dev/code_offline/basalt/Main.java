package dev.code_offline.basalt;

import dev.code_offline.basalt.model.note.Notes;
import dev.code_offline.basalt.view.MainFrame;

import javax.swing.*;
import java.util.ArrayList;

public class Main {
	public static boolean DEBUG = true;

	public static Notes notes = new Notes();

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainFrame::new);
	}

	public static String assetsPrefix(String path) {
		return "src/main/resources/assets/" + path;
	}
}
