package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.core.Icons;
import org.springframework.lang.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogPanel extends JPanel implements BasaltDockable {
	private final JTextArea logArena;
	private final JScrollPane logScroll;
	
	public LogPanel() {
		super(new BorderLayout());
		
		logArena = new JTextArea();
		logScroll = new JScrollPane(logArena);
	
		logArena.setEditable(false);
		
		add(logScroll, BorderLayout.CENTER);
		
		Main.LOGGER.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				logArena.append(record.getLevel() + ": " + record.getMessage() + "\n");
				JScrollBar vertical = logScroll.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());
			}
			
			@Override
			public void flush() {
			
			}
			
			@Override
			public void close() throws SecurityException {
			
			}
		});
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
