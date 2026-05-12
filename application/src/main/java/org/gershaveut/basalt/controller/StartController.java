package org.gershaveut.basalt.controller;

import org.gershaveut.basalt.model.database.Database;
import org.gershaveut.basalt.model.database.NetworkVersionException;
import org.gershaveut.basalt.model.database.ServerConnectException;
import org.gershaveut.basalt.model.recent.ApplicationRecentStarts;
import org.gershaveut.basalt.model.recent.RecentStart;
import org.gershaveut.basalt.view.ApplicationFrame;
import org.gershaveut.basalt.view.start.StartFrame;
import org.gershaveut.basalt.view.start.StartListener;
import org.gershaveut.basalt.view.start.UnknownException;
import org.gershaveut.basalt_server.SpringApplication;
import org.gershaveut.basalt_share.Util;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;

import javax.net.ssl.SSLException;
import javax.swing.*;
import java.io.File;
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
        var file = new File(path);
        
        if (!file.getName().endsWith(Util.APPLICATION_FORMAT)) {
            JOptionPane.showMessageDialog(startFrame, "Неправильный формат файла", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
		
		startFrame.setVisible(false);
		
		try {
			try {
				context = SpringApplication.startServer(List.of("--storage.path=" + file.getPath().split("\\.")[0]).toArray(new String[1]));
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
