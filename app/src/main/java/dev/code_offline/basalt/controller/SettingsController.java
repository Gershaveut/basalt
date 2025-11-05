package dev.code_offline.basalt.controller;

import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.settings.BasaltSettings;
import dev.code_offline.basalt.model.settings.SettingsModel;
import dev.code_offline.basalt.view.BasaltFrame;
import dev.code_offline.basalt.view.settings.SettingsFrame;
import dev.code_offline.basalt.view.settings.SettingsListener;
import dev.code_offline.basalt.view.tool.graph.GraphCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsController implements SettingsListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);
    
    private final SettingsFrame settingsFrame;
	private BasaltSettings basaltSettings;
	
	public SettingsController(SettingsFrame settingsFrame, BasaltFrame basaltFrame, GraphCanvas graphCanvas, Database database) {
        this.settingsFrame = settingsFrame;
        
        database.getClientPerson().subscribe(person -> {
            basaltSettings = new BasaltSettings(person);
            
            basaltSettings.getUsername().addSettingListener(value -> {
                database.renameClientPerson(value.toString());
            });
            
            basaltSettings.getPassword().addSettingListener(value -> {
                database.passwordClientPerson(value.toString(), person.getPassword()); // TODO: начать спрашивать старый пароль у пользователя
            });
            
            basaltSettings.getDescription().addSettingListener(value -> {
                database.descriptionClientPerson(value.toString());
            });
            
            basaltSettings.getMaxFps().addSettingListener(value -> {
                graphCanvas.setMaxFps(Integer.parseInt(value.toString()));
            });
            basaltSettings.getPhysicMaxFps().addSettingListener(value -> {
                graphCanvas.setPhysicMaxFps(Integer.parseInt(value.toString()));
            });
            
            basaltSettings.getDebugMode().addSettingListener(value -> {
                if ((Boolean) value) {
                    basaltFrame.enableDebug();
                }
            });
            basaltSettings.getDebugGenerateDatabase().addSettingListener(value -> {
                if ((Boolean) value) {
                    var debugDatabase = basaltFrame.database;
                    
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
                basaltSettings.loadSettings();
            } catch (Exception exception) {
                LOGGER.error("Error loading settings", exception);
            }
            
            settingsFrame.setModel(basaltSettings.getSettingsModel());
        });
    }
    
    @Override
    public void saveSettings(SettingsModel revertSettingsModel) {
       basaltSettings.saveSettings(revertSettingsModel);
    }
    
    @Override
    public void revertSettings(SettingsModel revertSettingsModel) {
        basaltSettings.revertSettings(revertSettingsModel);
        settingsFrame.setModel(revertSettingsModel);
    }
    
    @Override
    public void setValue(String name, Object value) {
        basaltSettings.getSettingsModel().getSettings().stream().filter(set -> set.getName().equals(name)).findFirst().orElseThrow().setValue(value);
    }
}
