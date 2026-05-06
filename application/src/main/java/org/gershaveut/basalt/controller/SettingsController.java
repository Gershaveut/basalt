package org.gershaveut.basalt.controller;

import org.gershaveut.basalt.ApplicationUtil;
import org.gershaveut.basalt.model.database.Database;
import org.gershaveut.basalt.model.settings.ApplicationSettings;
import org.gershaveut.basalt.model.settings.SettingsModel;
import org.gershaveut.basalt.model.settings.Theme;
import org.gershaveut.basalt.view.ApplicationFrame;
import org.gershaveut.basalt.view.settings.SettingsFrame;
import org.gershaveut.basalt.view.settings.SettingsListener;
import org.gershaveut.basalt.view.tool.graph.GraphCanvas;
import org.gershaveut.basalt_server.model.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.swing.*;

public class SettingsController implements SettingsListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);
    
    private final SettingsFrame settingsFrame;
	private final ApplicationSettings applicationSettings;
	
	public SettingsController(SettingsFrame settingsFrame) {
        this.settingsFrame = settingsFrame;
		
		applicationSettings = new ApplicationSettings();
		
		applicationSettings.getTheme().addSettingListener(value -> {
			var theme = Theme.valueOf(ApplicationUtil.fromDisplayName(value.toString()));
			
			theme.applyTheme();
		});
		
		settingsFrame.addSettingsListener(this);
		
		loadSettings();
	}
	
	public void loadApplicationSettings(ApplicationFrame applicationFrame, GraphCanvas graphCanvas, Database database) {
		database.getClientPerson().subscribe(person -> {
			applicationSettings.getUsername().addSettingListener(value -> {
				database.renameClientPerson(value.toString(), httpStatusCode -> {
					if (httpStatusCode == HttpStatus.CONFLICT) {
						ApplicationUtil.showErrorDialog(settingsFrame, "Имя пользователя уже занято!");
						
						return true;
					}
					
					return false;
				});
			});
			
			applicationSettings.getPassword().addSettingListener(value -> {
				var passwordField = new JPasswordField();
				int input = JOptionPane.showConfirmDialog(null, passwordField, "Текущий пароль", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				
				if (input == JOptionPane.OK_OPTION)
					database.passwordClientPerson(value.toString(), new String(passwordField.getPassword()), httpStatusCode -> {
						if (httpStatusCode == HttpStatus.BAD_REQUEST) {
							showErrorDialog("Ошибка смены пароля!");
							
							return true;
						}
						
						return false;
					});
			});
			
			applicationSettings.getDescription().addSettingListener(value -> {
				database.descriptionClientPerson(value.toString());
			});
			
			applicationSettings.getMaxFps().addSettingListener(value -> {
				graphCanvas.setMaxFps(Integer.parseInt(value.toString()));
			});
			applicationSettings.getPhysicMaxFps().addSettingListener(value -> {
				graphCanvas.setPhysicMaxFps(Integer.parseInt(value.toString()));
			});
            applicationSettings.getSpawnZone().addSettingListener(value -> {
                graphCanvas.setSpawnZone(Integer.parseInt(value.toString()));
                graphCanvas.updateGraph();
            });
			
			applicationSettings.getCommentsSize().addSettingListener(value -> {
				database.setCommentsSize(Integer.parseInt(value.toString()));
			});

			applicationSettings.getDebugMode().addSettingListener(value -> {
				if ((Boolean) value) {
					applicationFrame.enableDebug();
				}
			});
			applicationSettings.getDebugGenerateDatabase().addSettingListener(value -> {
				if ((Boolean) value) {
					var debugDatabase = applicationFrame.database;
					
					debugDatabase.getNotes().subscribe(notes -> {
						if (notes.size() < 20) {
							for (int i = 1; i < 25; i++) {
								var note = new Note(String.valueOf(i), null);
								
								note.setText(String.format("[[%d]]", i + 1));
								
								debugDatabase.addNote(note);
							}
						}
					});
				}
			});
			
			loadSettings();
		});
	}
	
	private void loadSettings() {
        LOGGER.info("Loading settings...");
        
        applicationSettings.loadSettings();
		
		settingsFrame.setModel(applicationSettings.getSettingsModel());
	}

	private void showErrorDialog(String message) {
		ApplicationUtil.showErrorDialog(settingsFrame, message);
	}
	
	@Override
    public void saveSettings(SettingsModel revertSettingsModel) {
       applicationSettings.saveSettings(revertSettingsModel);
    }
    
    @Override
    public void revertSettings(SettingsModel revertSettingsModel) {
        applicationSettings.revertSettings(revertSettingsModel);
        settingsFrame.setModel(revertSettingsModel);
    }
    
    @Override
    public void setValue(String name, Object value) {
        applicationSettings.getSettingsModel().getSettings().stream().filter(set -> set.getName().equals(name)).findFirst().orElseThrow().setValue(value);
    }
}
