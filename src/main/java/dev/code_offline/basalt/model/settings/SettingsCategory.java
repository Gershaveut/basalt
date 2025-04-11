package dev.code_offline.basalt.model.settings;

import java.util.HashSet;
import java.util.Set;

public class SettingsCategory {
    private final String name;
    private final Set<Setting> settings = new HashSet<>();

    public SettingsCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<Setting> getSettings() {
        return settings;
    }

    public void add(Setting setting) {
        settings.add(setting);
    }
}
