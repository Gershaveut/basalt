package dev.code_offline.basalt.model.settings;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.core.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class BasaltSettings {
	private final String FILE_NAME = "settings.json";
	
	private SettingsModel settingsModel;

	private final Setting maxFps;
	private final Setting physicMaxFps;
	
	private final Setting debugMode;
	private final Setting debugGenerateDatabase;
	
	public BasaltSettings() {
		var settingsTabs = new ArrayList<SettingsTab>();
		
		var generalTab = new SettingsTab("Основные", "Основные настройки программы");
		var toolTab = new SettingsTab("Инструменты");
		var miscTab = new SettingsTab("Разное");
	
		var graphCategory = new SettingsCategory("Граф");
		
		maxFps = new Setting("Частота кадров", null, "60");
		physicMaxFps = new Setting("Частота обновления физики", null, "120");
		
		var debugCategory = new SettingsCategory("Отладка", "Используйте на свой страх и риск!");
		
		debugMode = new Setting("Режим отладки", "После отключения отладки требуется перезагрузка!", false);
		debugGenerateDatabase = new Setting("Генерация базы данных", null, false);
	
		graphCategory.add(maxFps);
		graphCategory.add(physicMaxFps);
		toolTab.add(graphCategory);
		
		debugCategory.add(debugGenerateDatabase);
		debugCategory.add(debugMode);
		miscTab.add(debugCategory);
		
		settingsTabs.add(generalTab);
		settingsTabs.add(toolTab);
		settingsTabs.add(miscTab);
		
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
			Main.LOGGER.log(Level.INFO, "Save settings...");
			
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
				writer.write(Util.getMapper().writeValueAsString(settingsModel.getSettings()));
				
				writer.close();
			} catch (IOException e) {
				Main.LOGGER.log(Level.SEVERE, "Error save settings: " + e.getMessage());
			}
		}
	}
	
	public void revertSettings(SettingsModel revertSettingsModel) {
		settingsModel = revertSettingsModel;
	}
	
	public SettingsModel getSettingsModel() {
		return settingsModel;
	}
	
	public Setting getPhysicMaxFps() {
		return physicMaxFps;
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
