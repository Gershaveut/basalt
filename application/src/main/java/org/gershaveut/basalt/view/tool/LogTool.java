package org.gershaveut.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import org.gershaveut.basalt.ApplicationUtil;
import org.gershaveut.basalt.view.Icons;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class LogTool extends AbstractTool {
	private final JTextArea logArena;
	private final JScrollPane logScroll;
	
	public LogTool() {
		this.setLayout(new BorderLayout());
		
		this.setPreferredSize(ApplicationUtil.BOX_WINDOW_DIMENSION_TOOL);
		
		logArena = new JTextArea();
		logScroll = new JScrollPane(logArena);
	
		logArena.setEditable(false);
		
		add(logScroll, BorderLayout.CENTER);

		System.setOut(new PrintStream(new OutputStream() {
			final PrintStream out = System.out;
		
			@Override
			public void write(byte[] b, int off, int len) {
				var text = new String(b, off, len);
				
				logArena.append(text);
				JScrollBar vertical = logScroll.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());
				
				out.print(text);
			}
			
			@Override
			public void write(int b) {
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
	public int getDockingModes() {
		return DockingMode.ALL;
	}
	
	@Override
	public @Nullable ImageIcon getIconOriginal() {
		return Icons.TERMINAL.getIcon();
	}
}
