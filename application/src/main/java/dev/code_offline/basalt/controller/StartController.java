package dev.code_offline.basalt.controller;

import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.model.database.NetworkVersionException;
import dev.code_offline.basalt.model.database.ServerConnectException;
import dev.code_offline.basalt.model.recent.ApplicationRecentStarts;
import dev.code_offline.basalt.model.recent.RecentStart;
import dev.code_offline.basalt.view.ApplicationFrame;
import dev.code_offline.basalt.view.settings.SettingsFrame;
import dev.code_offline.basalt.view.start.StartFrame;
import dev.code_offline.basalt.view.start.StartListener;
import dev.code_offline.basalt.view.start.UnknownException;
import dev.code_offline.basalt_server.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;

import javax.net.ssl.SSLException;
import javax.swing.*;
import java.util.List;

public class StartController implements StartListener {
	private final ApplicationRecentStarts applicationRecentStarts;
	private final StartFrame startFrame;
	
	private @Nullable ConfigurableApplicationContext context;

	public StartController(ApplicationRecentStarts applicationRecentStarts, StartFrame startFrame) {
		this.applicationRecentStarts = applicationRecentStarts;
		this.startFrame = startFrame;
		
		startFrame.updateRecents(applicationRecentStarts.getRecentStarts());
		
		startFrame.addStartListener(this);
	}
	
	private void openApplicationFrame(Database database) {
		startFrame.setVisible(false);
		
		new ApplicationFrame(database, startFrame, this).setVisible(true);
	}
	
	private void addRecentStart(RecentStart recentStart) {
		applicationRecentStarts.addRecentStart(recentStart);
		startFrame.updateRecents(applicationRecentStarts.getRecentStarts());
	}
	
	@Override
	public void openDatabase(String path) {
		if (path.contains(".")) {
			path = path.substring(0, path.indexOf('.'));
		}
		
		startFrame.setVisible(false);
		
		try {
			try {
				context = SpringApplication.startServer(List.of("--spring.datasource.url=jdbc:h2:save:" + path).toArray(new String[1]));
			} catch (Exception exception) {
				throw new UnknownException(exception);
			}
			
			try {
				openApplicationFrame(new Database());
				
				addRecentStart(new RecentStart(path, true));
			} catch (ServerConnectException | NetworkVersionException | SSLException exception) {
				startFrame.setVisible(true);
				JOptionPane.showMessageDialog(startFrame, "Неизвестная ошибка", "Ошибка", JOptionPane.ERROR_MESSAGE);
				throw new UnknownException(exception);
			}
		} catch (UnknownException ignored) {
			if (context != null)
				context.close();
			
			startFrame.setVisible(true);
			JOptionPane.showMessageDialog(startFrame, "Ошибка при запуске сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void connectDatabase(String ip) {
		if (!Database.tryConnect(ip)) {
			JOptionPane.showMessageDialog(startFrame, "Не удалось подключиться", "Ошибка", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		JTextField username = new JTextField();
		JTextField password = new JPasswordField();
		Object[] message = {
				"Имя пользователя:", username,
				"Пароль:", password
		};
		
		var option = JOptionPane.showConfirmDialog(startFrame, message, "Аутентификация", JOptionPane.OK_CANCEL_OPTION);
		
		if (option == JOptionPane.OK_OPTION) {
			try {
				openApplicationFrame(new Database(ip, username.getText(), password.getText()));
				
				addRecentStart(new RecentStart(ip, false));
			} catch (NetworkVersionException ignored) {
				JOptionPane.showMessageDialog(startFrame, "Версии клиента и сервера не совпадают", "Ошибка", JOptionPane.ERROR_MESSAGE);
			} catch (ServerConnectException | SSLException ignored) {
				JOptionPane.showMessageDialog(startFrame, "Не удалось подключиться", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	@Override
	public void deleteRecentStart(RecentStart recentStart) {
		applicationRecentStarts.removeRecentStart(recentStart);
		startFrame.updateRecents(applicationRecentStarts.getRecentStarts());
	}
	
	public @Nullable ConfigurableApplicationContext getContext() {
		return context;
	}
}
