package dev.code_offline.basalt.controller;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.model.client.json.JSONClient;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.settings.Setting;
import dev.code_offline.basalt.model.settings.SettingsCategory;
import dev.code_offline.basalt.model.settings.SettingsModel;
import dev.code_offline.basalt.model.settings.SettingsTab;
import dev.code_offline.basalt.view.MainFrame;
import dev.code_offline.basalt.view.settings.SettingsFrame;
import dev.code_offline.basalt.view.settings.SettingsListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class SettingsController implements SettingsListener {
    private final String FILE_NAME = "settings.json";

    private final SettingsFrame settingsFrame;
    private SettingsModel settingsModel;

    public SettingsController(SettingsFrame settingsFrame, MainFrame mainFrame) {
        this.settingsFrame = settingsFrame;

        var settingsTabs = new HashSet<SettingsTab>();

        var generalTab = new SettingsTab("Основные", "Основные настройки программы");
        var serverCategory = new SettingsCategory("Сервер");

        var serverAddress = new Setting("Адрес сервера", "localhost:8080");

        serverCategory.add(serverAddress);
        generalTab.add(serverCategory);

        var miscTab = new SettingsTab("Разное");

        var debugCategory = new SettingsCategory("Отладка", "Используйте на свой страх и риск!");

        var debugMode = new Setting("Режим отладки", "После отключения отладки требуется перезагрузка!", false);
        debugMode.addSettingListener(value -> {
            if ((Boolean) value) {
                mainFrame.enableDebug();
            }
        });
        
        var debugGenerateDatabase = new Setting("Генерация базы данных", false);
        debugGenerateDatabase.addSettingListener(value -> {
            if ((Boolean) value) {
                var debugClient = mainFrame.client;
               
                if (debugClient.getNotes().size() < 20) {
                    for (int i = 1; i < 25; i++) {
                        var note = new Note(String.valueOf(i), debugClient.getClientPerson().getId(), debugClient.getRoot());
                        
                        note.setText(String.format("[%d]", i + 1));
                        
                        debugClient.addNote(note);
                    }
                }
            }
        });
        
        debugCategory.add(debugGenerateDatabase);
        debugCategory.add(debugMode);
        miscTab.add(debugCategory);

        settingsTabs.add(miscTab);
        settingsTabs.add(generalTab);

        settingsModel = new SettingsModel(settingsTabs);

        try {
            Main.logger.log(Level.INFO, "Loading settings...");
            loadSettings();
        } catch (Exception e) {
            Main.logger.log(Level.SEVERE, "Error loading settings: " + e.getMessage());
        }

        settingsFrame.setModel(settingsModel);

        settingsFrame.addSettingsListener(this);
    }

    private void loadSettings() throws Exception {
        var ignored = new File(FILE_NAME).createNewFile();

        var json = new Gson().fromJson(Files.readString(Path.of(FILE_NAME)), Setting[].class);

        if (json != null) {
            var settings = Arrays.stream(json).toList();

            settings.forEach(setting -> {
                var findSetting = settingsModel.getSettings().stream().filter(set -> set.hashCode() == setting.hashCode()).findFirst().orElse(null);

                if (findSetting != null) {
                    findSetting.setValue(setting.getValue());
                    findSetting.notifyListeners();
                }
            });
        }
    }

    @Override
    public void saveSettings(SettingsModel revertSettingsModel) {
        var changed = false;

        for (Setting setting : settingsModel.getSettings()) {
            if (revertSettingsModel.getSettings().stream().filter(set -> set.hashCode() == setting.hashCode()).findFirst().orElseThrow().getValue() != setting.getValue()) {
                setting.notifyListeners();
                changed = true;
            }
        }

        if (changed) {
            Main.logger.log(Level.INFO, "Save settings...");

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
                writer.write(new Gson().newBuilder().setFormattingStyle(FormattingStyle.PRETTY).create().toJson(settingsModel.getSettings()));

                writer.close();
            } catch (IOException e) {
                Main.logger.log(Level.SEVERE, "Error save settings: " + e.getMessage());
            }
        }
    }

    @Override
    public void revertSettings(SettingsModel revertSettingsModel) {
        settingsModel = revertSettingsModel;
        settingsFrame.setModel(settingsModel);
    }
}
