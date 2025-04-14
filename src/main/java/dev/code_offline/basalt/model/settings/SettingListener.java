package dev.code_offline.basalt.model.settings;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EventListener;

public interface SettingListener extends EventListener {
    void valueChanged(Object value);
}
