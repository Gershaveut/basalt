package dev.code_offline.basalt.view;

import dev.code_offline.basalt.controller.Database.Database;
import dev.code_offline.basalt.controller.client.Client;
import dev.code_offline.basalt_server.BasaltApplication;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.List;


public class StartFrame extends JFrame {
	public @Nullable ConfigurableApplicationContext context;
	
	public StartFrame() {
		this.setTitle("Basalt");
		this.setLayout(new BorderLayout());
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		var buttonPanel = new JPanel(new FlowLayout());
		
		var createDatabaseButton = new JButton("Создать");
		var openDatabaseButton = new JButton("Открыть");
		var connectDatabaseButton = new JButton("Подключиться");
		
		createDatabaseButton.addActionListener(e -> chooseDatabaseFile(true));
		openDatabaseButton.addActionListener(e -> chooseDatabaseFile(false));
		connectDatabaseButton.addActionListener(e -> {
			var ip = JOptionPane.showInputDialog(this, "Введите адрес сервера:", "Подключение", JOptionPane.PLAIN_MESSAGE);
		
			if (!ip.isEmpty()) {
				try {
					openDatabase(new Client(new Database(ip)));
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "Не удалось подключиться", "Ошибка", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
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
	
	private void chooseDatabaseFile(boolean create) {
		var fileChooser = new JFileChooser();
		
		fileChooser.setFileFilter(new FileNameExtensionFilter("База данных (.db)", "db"));
		
		int result;
		
		if (create) {
			result = fileChooser.showSaveDialog(this);
		} else {
			result = fileChooser.showOpenDialog(this);
		}
		
		if (result == JFileChooser.APPROVE_OPTION) {
			startServer(fileChooser.getSelectedFile().getPath());
		}
	}
	
	private void startServer(String path) {
		if (path.contains(".")) {
			path = path.substring(0, path.indexOf('.'));
		}
	
		setVisible(false);
		
		try {
			context = BasaltApplication.startServer(List.of("--spring.datasource.url=jdbc:h2:file:" + path).toArray(new String[1]));
			
			try {
				openDatabase(new Client(new Database()));
			} catch (Exception ignored) {
				JOptionPane.showMessageDialog(this, "Неизвестная ошибка", "Ошибка", JOptionPane.ERROR_MESSAGE);
				setVisible(true);
			}
		} catch (Exception ignored) {
			JOptionPane.showMessageDialog(this, "Ошибка при запуске сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
			setVisible(true);
		}
	}
	
	private void openDatabase(Client client) {
		setVisible(false);
		
		new MainFrame(client, this).setVisible(true);
	}
}
