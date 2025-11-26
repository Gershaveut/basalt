package dev.code_offline.basalt.view.start;

import dev.code_offline.basalt.ApplicationUtil;
import dev.code_offline.basalt.controller.StartController;
import dev.code_offline.basalt.model.recent.ApplicationRecentStarts;
import dev.code_offline.basalt.model.recent.RecentStart;
import dev.code_offline.basalt_share.Util;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;


public class StartFrame extends JFrame {
	private final EventListenerList listeners = new EventListenerList();
	
	private final JList<RecentStart> recentList;
	
	private final JPopupMenu popupMenu;
	
	public StartFrame() {
		this.setTitle(Util.APPLICATION_NAME);
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
				notifyListeners(startListener -> startListener.connectDatabase(ip));
			}
		});
		
		buttonPanel.add(createDatabaseButton);
		buttonPanel.add(openDatabaseButton);
		buttonPanel.add(connectDatabaseButton);
		
		recentList = new JList<>();
		var recentPanel = new JPanel(new BorderLayout());
		popupMenu = new JPopupMenu();
	
		var openItem = new JMenuItem("Открыть");
		var deleteItem = new JMenuItem("Удалить");

		ApplicationUtil.registerAccelerator(openItem, recentList, KeyStroke.getKeyStroke("ENTER"));
		ApplicationUtil.registerAccelerator(deleteItem, recentList, KeyStroke.getKeyStroke("DELETE"));
		
		openItem.addActionListener(e -> openRecentDatabase());
		deleteItem.addActionListener(e -> {
			notifyListeners(startListener -> startListener.deleteRecentStart(recentList.getSelectedValue()));
		});
		
		popupMenu.add(openItem);
		popupMenu.add(new JSeparator());
		popupMenu.add(deleteItem);
		
		var recentLabel = new JLabel("Недавние базы данных:");
		
		recentList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (isVisible()) {
					if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
						openRecentDatabase();
					} else if (SwingUtilities.isRightMouseButton(e)) {
						recentList.setSelectedIndex(recentList.locationToIndex(e.getPoint()));
						
						showPopupMenu(recentPanel, e.getX(), e.getY());
					}
				}
			}
		});
		
		recentList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (ApplicationUtil.isContextKey(e) && !recentList.isSelectionEmpty()) {
					showPopupMenu(recentPanel, 0, 0);
				}
			}
		});
		
		recentPanel.add(recentLabel, BorderLayout.NORTH);
		recentPanel.add(new JScrollPane(recentList), BorderLayout.CENTER);
		
		add(buttonPanel, BorderLayout.NORTH);
		
		add(recentPanel, BorderLayout.CENTER);
		
		new StartController(new ApplicationRecentStarts(), this);
		
		this.setVisible(true);
	}
	
	private void showPopupMenu(JPanel parent, int x, int y) {
		if (recentList.getModel().getSize() > 0)
			popupMenu.show(parent, x, y);
	}
	
	public void updateRecents(List<RecentStart> recentStartList) {
		var model = new DefaultListModel<RecentStart>();
		recentStartList.forEach(model::addElement);
		recentList.setModel(model);
	}
	
	private void openRecentDatabase() {
		var recentDatabase = recentList.getSelectedValue();
		
		if (recentDatabase.isOffline()) {
			notifyListeners(startListener -> startListener.openDatabase(recentDatabase.getAddress()));
		} else {
			notifyListeners(startListener -> startListener.connectDatabase(recentDatabase.getAddress()));
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
			
			if (result == JFileChooser.APPROVE_OPTION && !fileChooser.getSelectedFile().exists()) {
				JOptionPane.showMessageDialog(this, "Файл не найден!", "Ошибка", JOptionPane.ERROR_MESSAGE);
				
				return;
			}
		}
		
		if (result == JFileChooser.APPROVE_OPTION) {
			notifyListeners(startListener -> startListener.openDatabase(fileChooser.getSelectedFile().getPath()));
		}
	}
	
	public void addStartListener(StartListener startListener) {
		listeners.add(StartListener.class, startListener);
	}
	
	public void removeStartListener(StartListener startListener) {
		listeners.remove(StartListener.class, startListener);
	}
	
	private void notifyListeners(Consumer<StartListener> action) {
		Arrays.stream(listeners.getListeners(StartListener.class)).toList().forEach(action);
	}
}
