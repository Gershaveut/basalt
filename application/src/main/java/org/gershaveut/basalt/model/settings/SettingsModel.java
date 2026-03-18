package org.gershaveut.basalt.model.settings;

import java.util.ArrayList;
import java.util.List;

public class SettingsModel implements Cloneable {
    private List<SettingsTab> settingsTabs;

    public SettingsModel(List<SettingsTab> settingsModel) {
        this.settingsTabs = settingsModel;
    }

    public SettingsModel() {
        this(new ArrayList<>());
    }

    public List<SettingsTab> getSettingsTabs() {
        return settingsTabs;
    }

    public List<Setting> getSettings() {
        var settings = new ArrayList<Setting>();

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
            cloned.settingsTabs = new ArrayList<>();
            settingsTabs.forEach(tab -> cloned.settingsTabs.add(tab.clone()));
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable not implemented correctly", e);
        }
    }
}
