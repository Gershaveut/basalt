package dev.code_offline.basalt.model.settings;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.event.EventListenerList;
import java.util.Objects;

public class Setting implements Cloneable {
    private final String name;
    private final @Nullable String description;
    private final Object defaultValue;
    private @Nullable Object value;

    private final EventListenerList listeners = new EventListenerList();

    public Setting(String name, @Nullable String description, Object defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;

        this.value = this.defaultValue;
    }

    public Setting(String name, Object defaultValue) {
        this(name, null, defaultValue);
    }

    public String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

    public void notifyListeners() {
        for (SettingListener listener : listeners.getListeners(SettingListener.class)) {
            listener.valueChanged(value);
        }
    }

    public void addSettingListener(SettingListener settingListener) {
        listeners.add(SettingListener.class, settingListener);
    }

    public void removeSettingListener(SettingListener settingListener) {
        listeners.remove(SettingListener.class, settingListener);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Setting setting)) return false;
        return Objects.equals(name, setting.name) && Objects.equals(description, setting.description) && Objects.equals(defaultValue, setting.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, defaultValue);
    }

    @Override
    public Setting clone() {
        try {
            return (Setting) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable not implemented correctly", e);
        }
    }
}
