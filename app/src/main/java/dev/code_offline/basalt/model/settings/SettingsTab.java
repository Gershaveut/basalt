package dev.code_offline.basalt.model.settings;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SettingsTab implements Cloneable {
    private final String name;
    private final @Nullable String description;
    private Set<SettingsCategory> settingsCategories = new HashSet<>();

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

    @Override
    public SettingsTab clone() {
        try {
            SettingsTab cloned = (SettingsTab) super.clone();
            cloned.settingsCategories = new HashSet<>();
            settingsCategories.forEach(category -> cloned.settingsCategories.add(category.clone()));
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable not implemented correctly", e);
        }
    }
}
