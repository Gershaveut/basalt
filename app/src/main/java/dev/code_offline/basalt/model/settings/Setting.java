package dev.code_offline.basalt.model.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import javax.swing.event.EventListenerList;

public class Setting implements Cloneable {
    private final String name;
    @JsonIgnore
    private final @Nullable String description;
    @JsonIgnore
    private final Object defaultValue;
    
    private @Nullable Object value;
    @JsonIgnore
    private final boolean actionSetting;
    
    private final EventListenerList listeners = new EventListenerList();

    @JsonCreator
    protected Setting(@JsonProperty(value = "name", required = true) String name, @JsonProperty(value = "value", required = true) Object value) {
        this(name, null, value);
    }
    
    public Setting(String name, @Nullable String description, Object defaultValue, boolean actionSetting) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.actionSetting = actionSetting;
        
        this.value = this.defaultValue;
    }
    
    public Setting(String name, @Nullable String description, Object defaultValue) {
        this(name, description, defaultValue, false);
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
    
    public boolean isActionSetting() {
        return actionSetting;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

    public void notifyListeners() {
        for (SettingListener listener : listeners.getListeners(SettingListener.class)) {
            assert value != null;
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
    public Setting clone() {
        try {
            return (Setting) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloneable not implemented correctly", e);
        }
    }
}
