package dev.code_offline.basalt.model.settings;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SettingsTab {
    private final String name;
    private final @Nullable String description;
    private final Set<SettingsCategory> settingsCategories = new HashSet<>();

    public SettingsTab(String name, @Nullable String description) {
        this.name = name;
        this.description = description;
    }

    public SettingsTab(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public Set<SettingsCategory> getSettingsCategories() {
        return settingsCategories;
    }

    public void add(SettingsCategory settingsCategory) {
        settingsCategories.add(settingsCategory);
    }
}
