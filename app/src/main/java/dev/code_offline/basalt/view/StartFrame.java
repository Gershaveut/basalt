package dev.code_offline.basalt.view;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.controller.client.Client;
import dev.code_offline.basalt.model.RecentDatabase;
import dev.code_offline.basalt.model.database.NetworkVersionException;
import dev.code_offline.basalt.model.database.ServerConnectException;
import dev.code_offline.basalt_server.BasaltApplication;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StartFrame extends JFrame {
	private static final String FILE_NAME = "recents.json";
	
	private final JList<RecentDatabase> recentList;
	
	private List<RecentDatabase> recentDatabaseList = new ArrayList<>();
	
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
			
			if (ip != null && !ip.isEmpty()) {
				connectServer(ip);
			}
		});
		
		buttonPanel.add(createDatabaseButton);
		buttonPanel.add(openDatabaseButton);
		buttonPanel.add(connectDatabaseButton);
		
		var recentPanel = new JPanel(new BorderLayout());
		
		var recentLabel = new JLabel("Недавние базы данных:");
		recentList = new JList<>();
		
		recentList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					var recentDatabase = recentList.getSelectedValue();
					
					if (recentDatabase.isOffline()) {
						startServer(recentDatabase.getAddress());
					} else {
						connectServer(recentDatabase.getAddress());
					}
				}
			}
		});
		
		recentPanel.add(recentLabel, BorderLayout.NORTH);
		recentPanel.add(new JScrollPane(recentList), BorderLayout.CENTER);
		
		add(buttonPanel, BorderLayout.NORTH);
		
		add(recentPanel, BorderLayout.CENTER);
		
		try {
			loadRecents();
		} catch (Exception ignored) {
			Main.logger.severe("Error load recents");
		}
		
		this.setVisible(true);
	}
	
	private void loadRecents() throws Exception {
		var ignored = new File(FILE_NAME).createNewFile();
		
		var json = new Gson().fromJson(Files.readString(Path.of(FILE_NAME)), RecentDatabase[].class);
		
		if (json != null) {
			recentDatabaseList = new ArrayList<>(Arrays.stream(json).toList());
			updateRecents();
		}
	}
	
	private void saveRecents() throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
		writer.write(new Gson().newBuilder().setFormattingStyle(FormattingStyle.PRETTY).create().toJson(recentDatabaseList));
		
		writer.close();
	}
	
	private void updateRecents() {
		var model = new DefaultListModel<RecentDatabase>();
		recentDatabaseList.forEach(model::addElement);
		recentList.setModel(model);
	}
	
	private void addRecentDatabase(RecentDatabase recentDatabase) {
		if (recentDatabaseList.stream().noneMatch(d -> d.getAddress().equals(recentDatabase.getAddress()))) {
			recentDatabaseList.add(recentDatabase);
			updateRecents();
			
			try {
				saveRecents();
			} catch (Exception ignored) {
				Main.logger.severe("Error save recents");
			}
		}
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
				
				addRecentDatabase(new RecentDatabase(path, true));
			} catch (ServerConnectException | NetworkVersionException exception) {
				JOptionPane.showMessageDialog(this, "Неизвестная ошибка", "Ошибка", JOptionPane.ERROR_MESSAGE);
				throw exception;
			}
		} catch (Exception ignored) {
			if (context != null)
				context.close();
			
			JOptionPane.showMessageDialog(this, "Ошибка при запуске сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
			setVisible(true);
		}
	}
	
	private void connectServer(String ip) {
		try {
			openDatabase(new Client(new Database(ip)));
			
			addRecentDatabase(new RecentDatabase(ip, false));
		} catch (NetworkVersionException exception) {
			JOptionPane.showMessageDialog(this, "Версии клиента и сервера не совпадают", "Ошибка", JOptionPane.ERROR_MESSAGE);
		} catch (ServerConnectException ignored) {
			JOptionPane.showMessageDialog(this, "Не удалось подключиться", "Ошибка", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void openDatabase(Client client) {
		setVisible(false);
		
		new MainFrame(client, this).setVisible(true);
	}
}
