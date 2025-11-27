package dev.code_offline.basalt.controller;

import dev.code_offline.basalt.ApplicationUtil;
import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.model.settings.ApplicationSettings;
import dev.code_offline.basalt.model.settings.SettingsModel;
import dev.code_offline.basalt.view.ApplicationFrame;
import dev.code_offline.basalt.view.settings.SettingsFrame;
import dev.code_offline.basalt.view.settings.SettingsListener;
import dev.code_offline.basalt.view.tool.graph.GraphCanvas;
import dev.code_offline.basalt_share.model.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class SettingsController implements SettingsListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);
    
    private final SettingsFrame settingsFrame;
	private ApplicationSettings applicationSettings;
	
	public SettingsController(SettingsFrame settingsFrame, ApplicationFrame applicationFrame, GraphCanvas graphCanvas, Database database) {
        this.settingsFrame = settingsFrame;
        
        database.getClientPerson().subscribe(person -> {
            applicationSettings = new ApplicationSettings(person);
            
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
                database.passwordClientPerson(value.toString(), person.getPassword()); // TODO: начать спрашивать старый пароль у пользователя
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
            
            settingsFrame.addSettingsListener(this);
            
            try {
                LOGGER.info("Loading settings...");
                applicationSettings.loadSettings();
            } catch (Exception exception) {
                LOGGER.error("Error loading settings", exception);
            }
            
            settingsFrame.setModel(applicationSettings.getSettingsModel());
        });
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
