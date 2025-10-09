package dev.code_offline.basalt.model.settings;

import java.util.HashSet;
import java.util.Set;

public class SettingsModel implements Cloneable {
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

    public Set<Setting> getSettings() {
        var settings = new HashSet<Setting>();

        settingsTabs.forEach(tab -> {
            tab.getSettingsCategories().forEach(category -> {
                settings.addAll(category.getSettings());
            });
        });

        return settings;
    }

    @Override
    public SettingsModel clone() {
        try {
            SettingsModel cloned = (SettingsModel) super.clone();
            cloned.settingsTabs = new HashSet<>();
            settingsTabs.forEach(tab -> cloned.settingsTabs.add(tab.clone()));
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable not implemented correctly", e);
        }
    }
}
