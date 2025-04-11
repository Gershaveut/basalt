package dev.code_offline.basalt.model.settings;

import java.util.HashSet;
import java.util.Set;

public class SettingsModel {
    private Set<SettingsTab> settingsTabs;

    public SettingsModel(Set<SettingsTab> settingsModel) {
        this.settingsTabs = settingsModel;
    }

    public SettingsModel() {
        this(new HashSet<>());
    }

    public Set<SettingsTab> getSettingsTabs() {
        return settingsTabs;
    }
}
