package org.gershaveut.basalt.model.settings;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SettingsCategory implements Cloneable {
    private final String name;
    private final @Nullable String description;
    private List<Setting> settings = new ArrayList<>();

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

    public List<Setting> getSettings() {
        return settings;
    }

    public void add(Setting setting) {
        settings.add(setting);
    }

    @Override
    public SettingsCategory clone() {
        try {
            SettingsCategory cloned = (SettingsCategory) super.clone();
            cloned.settings = new ArrayList<>();
            settings.forEach(setting -> cloned.settings.add(setting.clone()));
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable not implemented correctly", e);
        }
    }
}
