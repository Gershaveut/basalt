package dev.code_offline.basalt.controller;

import com.google.gson.Gson;
import dev.code_offline.basalt.model.settings.Setting;
import dev.code_offline.basalt.model.settings.SettingsCategory;
import dev.code_offline.basalt.model.settings.SettingsModel;
import dev.code_offline.basalt.model.settings.SettingsTab;
import dev.code_offline.basalt.view.settings.SettingsFrame;
import dev.code_offline.basalt.view.settings.SettingsListener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsController implements SettingsListener {
    private final String FILE_NAME = "settings.json";

    private final SettingsFrame settingsFrame;
    private SettingsModel settingsModel;

    public SettingsController(SettingsFrame settingsFrame) {
        this.settingsFrame = settingsFrame;

        var settingsTabs = new HashSet<SettingsTab>();

        var generalTab = new SettingsTab("Основные", "Основные настройки программы");

        var serverCategory = new SettingsCategory("Сервер", "Настройки подключаемого сервера");

        var serverIP = new Setting("Адрес сервера", "Куда подключаться клиенту", "localhost:8080");
        serverIP.addSettingListener(System.out::println);

        serverCategory.add(serverIP);

        generalTab.add(serverCategory);

        settingsTabs.add(generalTab);

        settingsModel = new SettingsModel(settingsTabs);

        try {
            loadSettings();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        settingsFrame.setModel(settingsModel);

        settingsFrame.addSettingsListener(this);
    }

    private void loadSettings() throws IOException {
        var ignored = new File(FILE_NAME).createNewFile();

        var json = new Gson().fromJson(Files.readString(Path.of(FILE_NAME)), Setting[].class);

        if (json != null) {
            var settings = Arrays.stream(json).toList();

            settings.forEach(setting -> {
                var findSetting = settingsModel.getSettings().stream().filter(set -> set.hashCode() == setting.hashCode()).findFirst().orElseThrow();

                findSetting.setValue(setting.getValue());
                findSetting.notifyListeners();
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
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
                writer.write(new Gson().toJson(settingsModel.getSettings()));

                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void revertSettings(SettingsModel revertSettingsModel) {
        settingsModel = revertSettingsModel;
        settingsFrame.setModel(settingsModel);
    }
}
