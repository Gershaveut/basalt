package org.gershaveut.basalt.view.start;

import org.gershaveut.basalt.ApplicationUtil;
import org.gershaveut.basalt.controller.SettingsController;
import org.gershaveut.basalt.controller.StartController;
import org.gershaveut.basalt.model.recent.ApplicationRecentStarts;
import org.gershaveut.basalt.model.recent.RecentStart;
import org.gershaveut.basalt.view.settings.SettingsFrame;
import org.gershaveut.basalt_share.Util;
import org.apache.commons.text.WordUtils;

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
	
	public final SettingsFrame settingsFrame = new SettingsFrame();
	public final SettingsController settingsController = new SettingsController(settingsFrame);
	
	private final JList<RecentStart> recentList;
	
	private final JPopupMenu popupMenu;
	
	public StartFrame(String[] args) {
		this.setTitle(WordUtils.capitalize(Util.APPLICATION_NAME));
		this.setLayout(new BorderLayout());
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		var buttonPanel = new JPanel(new FlowLayout());
		
		var createDatabaseButton = new JButton("Создать");
		var openDatabaseButton = new JButton("Открыть");
		var connectDatabaseButton = new JButton("Подключиться");
		var settingsButton = new JButton("Настройки");
		
		createDatabaseButton.addActionListener(e -> chooseDatabaseFile(true));
		openDatabaseButton.addActionListener(e -> chooseDatabaseFile(false));
		connectDatabaseButton.addActionListener(e -> {
			var ip = JOptionPane.showInputDialog(this, "Введите адрес сервера:", "Подключение", JOptionPane.PLAIN_MESSAGE);
			
			if (ip != null && !ip.isEmpty()) {
				notifyListeners(startListener -> startListener.connectDatabase(ip));
			}
		});
		settingsButton.addActionListener(e -> {
			settingsFrame.setVisible(true);
		});
		
		buttonPanel.add(createDatabaseButton);
		buttonPanel.add(openDatabaseButton);
		buttonPanel.add(connectDatabaseButton);
		buttonPanel.add(settingsButton);
		
		recentList = new JList<>();
		var recentPanel = new JPanel(new BorderLayout());
		popupMenu = new JPopupMenu();
	
		var openItem = new JMenuItem("Открыть");
		var deleteItem = new JMenuItem("Удалить");

		ApplicationUtil.registerAccelerator(openItem, recentList, KeyStroke.getKeyStroke("ENTER"), null);
		ApplicationUtil.registerAccelerator(deleteItem, recentList, KeyStroke.getKeyStroke("DELETE"), null);
		
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

		recentList.setDropMode(DropMode.USE_SELECTION);
        recentList.setTransferHandler(new StartTransferHandler(listeners));

		recentPanel.add(recentLabel, BorderLayout.NORTH);
		recentPanel.add(new JScrollPane(recentList), BorderLayout.CENTER);
		
		add(buttonPanel, BorderLayout.NORTH);
		
		add(recentPanel, BorderLayout.CENTER);
		
		new StartController(new ApplicationRecentStarts(), this);
		
		this.setVisible(true);
	
		SwingUtilities.invokeLater(() -> {
			if (args.length > 0) {
				notifyListeners(startListener -> startListener.openDatabase(String.join(" ", args)));
			}
		});
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
		
		fileChooser.setFileFilter(new FileNameExtensionFilter(String.format("Базальт (%s)", Util.APPLICATION_FORMAT), Util.APPLICATION_FORMAT.substring(1)));
		
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
			var selectedFile = fileChooser.getSelectedFile().getPath(); 
			
			if (create)
				selectedFile = ApplicationUtil.ensureEndsWith(selectedFile, Util.APPLICATION_FORMAT);

			var finalSelectedFile = selectedFile;
			notifyListeners(startListener -> startListener.openDatabase(finalSelectedFile));
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
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		this.setEnabled(visible); //TODO: временное решение
	}
}
