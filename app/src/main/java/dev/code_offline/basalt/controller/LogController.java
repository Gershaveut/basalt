package dev.code_offline.basalt.controller;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.view.tool.LogPanel;

import javax.swing.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogController {
	public LogController(LogPanel logPanel) {
		Main.logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				logPanel.getLogArena().append(record.getLevel() + ": " + record.getMessage() + "\n");
				JScrollBar vertical = logPanel.getLogScroll().getVerticalScrollBar();
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
}
