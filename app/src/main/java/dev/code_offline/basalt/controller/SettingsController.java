package dev.code_offline.basalt.controller;

import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.settings.BasaltSettings;
import dev.code_offline.basalt.model.settings.SettingsModel;
import dev.code_offline.basalt.view.BasaltFrame;
import dev.code_offline.basalt.view.settings.SettingsFrame;
import dev.code_offline.basalt.view.settings.SettingsListener;

public class SettingsController implements SettingsListener {
    private final SettingsFrame settingsFrame;
	private final BasaltSettings basaltSettings;
	
	public SettingsController(SettingsFrame settingsFrame, BasaltFrame basaltFrame, BasaltSettings basaltSettings) {
        this.settingsFrame = settingsFrame;
		this.basaltSettings = basaltSettings;
		
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
                            var note = new Note(String.valueOf(i), 1, null);
                            
                            note.setText(String.format("[%d]", i + 1));
                            
                            debugDatabase.addNote(note);
                        }
                    }
                });
            }
        });
        

        settingsFrame.setModel(basaltSettings.getSettingsModel());
        settingsFrame.addSettingsListener(this);
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
}
