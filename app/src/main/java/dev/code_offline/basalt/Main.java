package dev.code_offline.basalt;

import dev.code_offline.basalt.controller.client.Database;
import dev.code_offline.basalt.view.StartFrame;

import javax.swing.*;
import java.util.logging.Logger;

public class Main {
	public static Logger logger = Logger.getGlobal();

	public static void main(String[] args) {
		testDatabase();
		
		// SwingUtilities.invokeLater(StartFrame::new);
	}
	
	private static void testDatabase() {
		var database = new Database();
		
		var notes = database.getNotes();
		
		System.out.println(notes);
		System.out.println(database.getPersons());
		System.out.println(database.getFolders());
	}
}
