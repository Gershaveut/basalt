package org.gershaveut.basalt.model.settings;

import org.gershaveut.basalt_share.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationSettings {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSettings.class);
	
	private static final String FILE_NAME = Util.savePrefix("settings.json");
	private SettingsModel settingsModel;
	
	private final Setting theme;
	private final Setting commentsSize;
	
	private final Setting username;
	private final Setting password;
	private final Setting description;
	
	private final Setting maxFps;
	private final Setting physicMaxFps;
    private final Setting spawnZone;
	
	private final Setting debugMode;
	private final Setting debugGenerateDatabase;
	
	public ApplicationSettings() {
		var settingsTabs = new ArrayList<SettingsTab>();
		
		var generalTab = new SettingsTab("Основные", "Основные настройки программы");
		var toolTab = new SettingsTab("Инструменты");
		var accountTab = new SettingsTab("Аккаунт");
		var miscTab = new SettingsTab("Разное");
	
		var appearanceCategory = new SettingsCategory("Внешний вид");
		
		theme = new Setting("Тема", null, Theme.WHITE);
		
		var accountCategory = new SettingsCategory("Аккаунт");
		
		username = new Setting("Имя пользователя", null, "", true, false);
		password = new Setting("Пароль", null, "", true, true);
		description = new Setting("Описание", null, "", true, false);
		
		var graphCategory = new SettingsCategory("Граф");
		
		maxFps = new Setting("Частота кадров", null, "60");
		physicMaxFps = new Setting("Частота обновления физики", null, "120");
		spawnZone = new Setting("Область появления нод", null, "600");
       
		var noteCategory = new SettingsCategory("Записка");

		commentsSize = new Setting("Количество отображаемых комментариев", null, "20");
		
		var debugCategory = new SettingsCategory("Отладка", "Используйте на свой страх и риск!");
		
		debugMode = new Setting("Режим отладки", "После отключения отладки требуется перезагрузка!", false);
		debugGenerateDatabase = new Setting("Генерация базы данных", null, false);
	
		appearanceCategory.add(theme);
		generalTab.add(appearanceCategory);
		
		accountCategory.add(username);
		accountCategory.add(password);
		accountCategory.add(description);
		accountTab.add(accountCategory);
		
		graphCategory.add(maxFps);
		graphCategory.add(physicMaxFps);
        graphCategory.add(spawnZone);
		noteCategory.add(commentsSize);
		toolTab.add(graphCategory);
		toolTab.add(noteCategory);
		
		//debugCategory.add(debugGenerateDatabase);
		debugCategory.add(debugMode);
		miscTab.add(debugCategory);
		
		settingsTabs.add(generalTab);
		settingsTabs.add(accountTab);
		settingsTabs.add(toolTab);
		settingsTabs.add(miscTab);
		
		settingsModel = new SettingsModel(settingsTabs);
	}
	
	public void loadSettings() {
        Setting[] json = null;
        List<Setting> findSettings;
                    
        try {
            var ignored = new File(FILE_NAME).createNewFile();
            json = Util.getMapper().readValue(Files.readString(Path.of(FILE_NAME)), Setting[].class);
        } catch (Exception exception) {
            LOGGER.error("Error load settings", exception);
        }

        if (json != null) {
            findSettings = Arrays.stream(json).toList();
        } else {
            findSettings = null;
            saveSettings(null);
        }

        var settings = settingsModel.getSettings();
                
        settings.forEach(setting -> {
            if (!setting.isActionSetting()) {
                var value = setting.getDefaultValue();
						
                if (findSettings != null) {
					var findSetting = findSettings.stream().filter(set -> set.getName().equals(setting.getName())).findFirst().orElse(null);
                    
					if (findSetting != null)
						value = findSetting.getValue();
                }
                        
                if (value != null && setting.getDefaultValue() instanceof Enum<?> anEnum) { // починка enum в настройках
                    value = Enum.valueOf(anEnum.getDeclaringClass(), value.toString().toUpperCase());
                }
						
                setting.setValue(value);
                setting.notifyListeners();
            }
        });
	}
	
	public void saveSettings(@Nullable SettingsModel revertSettingsModel) {
		var changed = false;
	
        if (revertSettingsModel != null) {
            for (Setting setting : settingsModel.getSettings()) {
                if (revertSettingsModel.getSettings().stream().filter(set -> set.getName().equals(setting.getName())).findFirst().orElseThrow().getValue() != setting.getValue()) {
                    setting.notifyListeners();
                    changed = true;
                }
            }
        }
		
		if (revertSettingsModel == null || changed) {
			LOGGER.info("Save settings...");
			
			try {
				var settings = settingsModel.getSettings().stream().filter(setting -> !setting.isActionSetting()).toList();
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
				writer.write(Util.getMapper().writeValueAsString(settings));
				
				writer.close();
			} catch (IOException exception) {
				LOGGER.error("Error save settings", exception);
			}
		}
	}
	
	public void revertSettings(SettingsModel revertSettingsModel) {
		settingsModel = revertSettingsModel;
	}
	
	public SettingsModel getSettingsModel() {
		return settingsModel;
	}
	
	public Setting getTheme() {
		return theme;
	}

	public Setting getCommentsSize() {
		return commentsSize;
	}

	public Setting getUsername() {
		return username;
	}
	
	public Setting getPassword() {
		return password;
	}
	
	public Setting getDescription() {
		return description;
	}
	
	public Setting getPhysicMaxFps() {
		return physicMaxFps;
	}

    public Setting getSpawnZone() {
        return spawnZone;
    }
    
	public Setting getMaxFps() {
		return maxFps;
	}
	
	public Setting getDebugMode() {
		return debugMode;
	}
	
	public Setting getDebugGenerateDatabase() {
		return debugGenerateDatabase;
	}
}
