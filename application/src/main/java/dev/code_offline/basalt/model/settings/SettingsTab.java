package dev.code_offline.basalt.model.settings;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SettingsTab implements Cloneable {
    private final String name;
    private final @Nullable String description;
    private List<SettingsCategory> settingsCategories = new ArrayList<>();

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

    public List<SettingsCategory> getSettingsCategories() {
        return settingsCategories;
    }

    public void add(SettingsCategory settingsCategory) {
        settingsCategories.add(settingsCategory);
    }

    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public SettingsTab clone() {
        try {
            SettingsTab cloned = (SettingsTab) super.clone();
            cloned.settingsCategories = new ArrayList<>();
            settingsCategories.forEach(category -> cloned.settingsCategories.add(category.clone()));
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable not implemented correctly", e);
        }
    }
}
