package dev.code_offline.basalt.model.settings;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SettingsCategory {
    private final String name;
    private final @Nullable String description;
    private final Set<Setting> settings = new HashSet<>();

    public SettingsCategory(String name, @Nullable String description) {
        this.name = name;
        this.description = description;
    }

    public SettingsCategory(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public Set<Setting> getSettings() {
        return settings;
    }

    public void add(Setting setting) {
        settings.add(setting);
    }
}
