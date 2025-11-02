package dev.code_offline.basalt.controller;

import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.model.database.NetworkVersionException;
import dev.code_offline.basalt.model.database.ServerConnectException;
import dev.code_offline.basalt.model.recent.BasaltRecentStarts;
import dev.code_offline.basalt.model.recent.RecentStart;
import dev.code_offline.basalt.view.BasaltFrame;
import dev.code_offline.basalt.view.start.StartFrame;
import dev.code_offline.basalt.view.start.StartListener;
import dev.code_offline.basalt_server.BasaltApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;

import javax.swing.*;
import java.util.List;

public class StartController implements StartListener {
	private final BasaltRecentStarts basaltRecentStarts;
	private final StartFrame startFrame;
	
	private @Nullable ConfigurableApplicationContext context;

	public StartController(BasaltRecentStarts basaltRecentStarts, StartFrame startFrame) {
		this.basaltRecentStarts = basaltRecentStarts;
		this.startFrame = startFrame;
		
		startFrame.updateRecents(basaltRecentStarts.getRecentStarts());
		
		startFrame.addStartListener(this);
	}
	
	private void openBasaltFrame(Database database) {
		startFrame.setVisible(false);
		
		new BasaltFrame(database, startFrame, this).setVisible(true);
	}
	
	private void addRecentStart(RecentStart recentStart) {
		basaltRecentStarts.addRecentStart(recentStart);
		startFrame.updateRecents(basaltRecentStarts.getRecentStarts());
	}
	
	@Override
	public void openDatabase(String path) {
		if (path.contains(".")) {
			path = path.substring(0, path.indexOf('.'));
		}
		
		startFrame.setVisible(false);
		
		try {
			context = BasaltApplication.startServer(List.of("--spring.datasource.url=jdbc:h2:file:" + path).toArray(new String[1]));
			
			try {
				openBasaltFrame(new Database());
				
				addRecentStart(new RecentStart(path, true));
			} catch (ServerConnectException | NetworkVersionException exception) {
				startFrame.setVisible(true);
				JOptionPane.showMessageDialog(startFrame, "Неизвестная ошибка", "Ошибка", JOptionPane.ERROR_MESSAGE);
				throw exception;
			}
		} catch (Exception ignored) {
			if (context != null)
				context.close();
			
			startFrame.setVisible(true);
			JOptionPane.showMessageDialog(startFrame, "Ошибка при запуске сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void connectDatabase(String ip) {
		JTextField username = new JTextField();
		JTextField password = new JPasswordField();
		Object[] message = {
				"Имя пользователя:", username,
				"Пароль:", password
		};
		
		var option = JOptionPane.showConfirmDialog(startFrame, message, "Аутентификация", JOptionPane.OK_CANCEL_OPTION);
		
		if (option == JOptionPane.OK_OPTION) {
			try {
				openBasaltFrame(new Database(ip, username.getText(), password.getText()));
				
				addRecentStart(new RecentStart(ip, false));
			} catch (NetworkVersionException exception) {
				JOptionPane.showMessageDialog(startFrame, "Версии клиента и сервера не совпадают", "Ошибка", JOptionPane.ERROR_MESSAGE);
			} catch (ServerConnectException ignored) {
				JOptionPane.showMessageDialog(startFrame, "Не удалось подключиться", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	@Override
	public void deleteRecentStart(RecentStart recentStart) {
		basaltRecentStarts.removeRecentStart(recentStart);
		startFrame.updateRecents(basaltRecentStarts.getRecentStarts());
	}
	
	public @Nullable ConfigurableApplicationContext getContext() {
		return context;
	}
}
