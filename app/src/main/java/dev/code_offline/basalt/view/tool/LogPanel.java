package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import org.springframework.lang.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogPanel extends JPanel implements BasaltDockable {
	private final JTextArea logArena;
	private final JScrollPane logScroll;
	
	public LogPanel() {
		super(new BorderLayout());
		
		logArena = new JTextArea();
		logScroll = new JScrollPane(logArena);
	
		logArena.setEditable(false);
		
		add(logScroll, BorderLayout.CENTER);

		System.setOut(new PrintStream(new OutputStream() {
			final PrintStream out = System.out;
			
			@Override
			public void write(int b) {
				var text = String.valueOf((char) b);
				
				logArena.append(text);
				JScrollBar vertical = logScroll.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());
				
				out.print(text);
			}
		}));
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
