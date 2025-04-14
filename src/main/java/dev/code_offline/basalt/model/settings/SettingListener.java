package dev.code_offline.basalt.model.settings;

import java.util.EventListener;

public interface SettingListener extends EventListener {
    void valueChanged(Object value);
}
