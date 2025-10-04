package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.core.Icons;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogPanel extends JPanel implements BasaltDockable {
	public LogPanel() {
		super(new BorderLayout());
		
		var logArea = new JTextArea();
		var logScroll = new JScrollPane(logArea);
	
		logArea.setEditable(false);
		
		Main.logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				logArea.append(record.getLevel() + ": " + record.getMessage() + "\n");
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
		
		add(logScroll, BorderLayout.CENTER);
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
