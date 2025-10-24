package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import java.awt.*;

public class LogPanel extends JPanel implements BasaltDockable {
	private final JTextArea logArena;
	private final JScrollPane logScroll;
	
	public LogPanel() {
		super(new BorderLayout());
		
		logArena = new JTextArea();
		logScroll = new JScrollPane(logArena);
	
		logArena.setEditable(false);
		
		add(logScroll, BorderLayout.CENTER);
	}
	
	public JTextArea getLogArena() {
		return logArena;
	}
	
	public JScrollPane getLogScroll() {
		return logScroll;
	}
	
	@Override
	public String getID() {
		return "log";
	}
	
	@Override
	public String getTitle() {
		return "Логи";
	}
	
	@Override
	public Component getContent() {
		return this;
	}
	
	@Override
	public int getDockingModes() {
		return DockingMode.ALL;
	}
	
	@Override
	public @Nullable ImageIcon getIconOriginal() {
		return Icons.TERMINAL.getIcon();
	}
}
