package dev.code_offline.basalt.model.settings;

import dev.code_offline.basalt.ApplicationUtil;
import dev.code_offline.basalt_share.Util;
import dev.code_offline.basalt_share.model.Person;
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

public class ApplicationSettings {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSettings.class);
	
	private static final String FILE_NAME = Util.savePrefix("settings.json");
	
	private SettingsModel settingsModel;

	private final Setting username;
	private final Setting password;
	private final Setting description;
	
	private final Setting maxFps;
	private final Setting physicMaxFps;
	
	private final Setting debugMode;
	private final Setting debugGenerateDatabase;
	
	public ApplicationSettings(Person clientPerson) {
		var settingsTabs = new ArrayList<SettingsTab>();
		
		var generalTab = new SettingsTab("Основные", "Основные настройки программы");
		var toolTab = new SettingsTab("Инструменты");
		var accountTab = new SettingsTab("Аккаунт");
		var miscTab = new SettingsTab("Разное");
	
		var accountCategory = new SettingsCategory("Аккаунт");
		
		var defaultDescription = clientPerson.getDescription();
		
		if (defaultDescription == null)
			defaultDescription = "";
		
		username = new Setting("Имя пользователя", null, clientPerson.getUsername(), true, false);
		password = new Setting("Пароль", null, "", true, true);
		description = new Setting("Описание", null, defaultDescription, true, false);
		
		var graphCategory = new SettingsCategory("Граф");
		
		maxFps = new Setting("Частота кадров", null, "60");
		physicMaxFps = new Setting("Частота обновления физики", null, "120");
		
		var debugCategory = new SettingsCategory("Отладка", "Используйте на свой страх и риск!");
		
		debugMode = new Setting("Режим отладки", "После отключения отладки требуется перезагрузка!", false);
		debugGenerateDatabase = new Setting("Генерация базы данных", null, false);
	
		accountCategory.add(username);
		accountCategory.add(password);
		accountCategory.add(description);
		accountTab.add(accountCategory);
		
		graphCategory.add(maxFps);
		graphCategory.add(physicMaxFps);
		toolTab.add(graphCategory);
		
		debugCategory.add(debugGenerateDatabase);
		debugCategory.add(debugMode);
		miscTab.add(debugCategory);
		
		//settingsTabs.add(generalTab); TODO: пустой таб
		settingsTabs.add(accountTab);
		settingsTabs.add(toolTab);
		settingsTabs.add(miscTab);
		
		settingsModel = new SettingsModel(settingsTabs);
	}
	
	public void loadSettings() throws Exception {
		var created = new File(FILE_NAME).createNewFile();
		
		if (!created) {
			var json = ApplicationUtil.getMapper().readValue(Files.readString(Path.of(FILE_NAME)), Setting[].class);
			
			if (json != null) {
				var settings = Arrays.stream(json).toList();
				
				settings.forEach(setting -> {
					Setting findSetting = settingsModel.getSettings().stream().filter(set -> set.getName().equals(setting.getName())).findFirst().orElse(null);
					
					if (findSetting != null && !findSetting.isActionSetting()) {
						findSetting.setValue(setting.getValue());
						findSetting.notifyListeners();
					}
				});
			}
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
			LOGGER.info("Save settings...");
			
			try {
				var settings = settingsModel.getSettings().stream().filter(setting -> !setting.isActionSetting()).toList();
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
				writer.write(ApplicationUtil.getMapper().writeValueAsString(settings));
				
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
