package dev.code_offline.basalt.controller;

import dev.code_offline.basalt.model.settings.Setting;
import dev.code_offline.basalt.model.settings.SettingsCategory;
import dev.code_offline.basalt.model.settings.SettingsModel;
import dev.code_offline.basalt.model.settings.SettingsTab;
import dev.code_offline.basalt.view.SettingsFrame;

import java.util.HashSet;

public class SettingsController {
    public SettingsController(SettingsFrame settingsFrame) {
        var settingsTabs = new HashSet<SettingsTab>();

        var generalTab = new SettingsTab("Основные", "Основные настройки программы");

        var serverCategory = new SettingsCategory("Сервер", "Настройки подключаемого сервера");

        var serverIP = new Setting("Адрес сервера", "Куда подключаться клиенту", "localhost:8080");
        serverIP.addSettingListener(System.out::println);

        serverCategory.add(serverIP);

        generalTab.add(serverCategory);

        settingsTabs.add(generalTab);

        settingsFrame.setModel(new SettingsModel(settingsTabs));
    }
}
