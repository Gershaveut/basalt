package dev.code_offline.basalt.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StartFrame extends JFrame {
	public StartFrame() {
		this.setTitle("Базальт");
		this.setLayout(new BorderLayout());
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		var buttonPanel = new JPanel(new FlowLayout());
		
		var createDatabaseButton = new JButton("Создать");
		var openDatabaseButton = new JButton("Открыть");
		var connectDatabaseButton = new JButton("Подключиться");
		
		buttonPanel.add(createDatabaseButton);
		buttonPanel.add(openDatabaseButton);
		buttonPanel.add(connectDatabaseButton);
		
		var recentPanel = new JPanel(new BorderLayout());
		
		var recentLabel = new JLabel("Недавние базы данных:");
		var recentList = new JList<String>();
		
		recentPanel.add(recentLabel, BorderLayout.NORTH);
		recentPanel.add(new JScrollPane(recentList), BorderLayout.CENTER);
		
		add(buttonPanel, BorderLayout.NORTH);
		add(recentPanel, BorderLayout.CENTER);
		
		this.setVisible(true);
	}
}
