package dev.code_offline.basalt.model.settings;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.core.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class BasaltSettings {
	private final String FILE_NAME = "settings.json";
	
	private SettingsModel settingsModel;
	
	private final Setting debugMode;
	private final Setting debugGenerateDatabase;
	
	public BasaltSettings() {
		var settingsTabs = new HashSet<SettingsTab>();
		
		var generalTab = new SettingsTab("Основные", "Основные настройки программы");
		
		var miscTab = new SettingsTab("Разное");
		
		var debugCategory = new SettingsCategory("Отладка", "Используйте на свой страх и риск!");
		
		debugMode = new Setting("Режим отладки", "После отключения отладки требуется перезагрузка!", false);
		debugGenerateDatabase = new Setting("Генерация базы данных", false);
		
		debugCategory.add(debugGenerateDatabase);
		debugCategory.add(debugMode);
		miscTab.add(debugCategory);
		
		settingsTabs.add(miscTab);
		settingsTabs.add(generalTab);
		
		settingsModel = new SettingsModel(settingsTabs);
	}
	
	public void loadSettings() throws Exception {
		var ignored = new File(FILE_NAME).createNewFile();
		
		var json = Util.getMapper().readValue(Files.readString(Path.of(FILE_NAME)), Setting[].class);
		
		if (json != null) {
			var settings = Arrays.stream(json).toList();
			
			settings.forEach(setting -> {
				Setting findSetting = settingsModel.getSettings().stream().filter(set -> set.getName().equals(setting.getName())).findFirst().orElse(null);
				
				if (findSetting != null) {
					findSetting.setValue(setting.getValue());
					findSetting.notifyListeners();
				}
			});
		}
	}
	
	public void saveSettings(SettingsModel revertSettingsModel) {
		var changed = false;
		
		for (Setting setting : settingsModel.getSettings()) {
			if (revertSettingsModel.getSettings().stream().filter(set -> set.getName().equals(setting.getName())).findFirst().orElseThrow().getValue() != setting.getValue()) {
				setting.notifyListeners();
				changed = true;
			}
		}
		
		if (changed) {
			Main.logger.log(Level.INFO, "Save settings...");
			
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
				writer.write(Util.getMapper().writeValueAsString(settingsModel.getSettings()));
				
				writer.close();
			} catch (IOException e) {
				Main.logger.log(Level.SEVERE, "Error save settings: " + e.getMessage());
			}
		}
	}
	
	public void revertSettings(SettingsModel revertSettingsModel) {
		settingsModel = revertSettingsModel;
	}
	
	public SettingsModel getSettingsModel() {
		return settingsModel;
	}
	
	public Setting getDebugMode() {
		return debugMode;
	}
	
	public Setting getDebugGenerateDatabase() {
		return debugGenerateDatabase;
	}
}
