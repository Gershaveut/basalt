package dev.code_offline.basalt.model.settings;

import java.util.HashSet;
import java.util.Set;

public class SettingsTab {
    private final String name;
    private final Set<SettingsCategory> settingsCategories = new HashSet<>();

    public SettingsTab(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<SettingsCategory> getSettingsCategories() {
        return settingsCategories;
    }

    public void add(SettingsCategory settingsCategory) {
        settingsCategories.add(settingsCategory);
    }
}
