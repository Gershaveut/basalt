package dev.code_offline.basalt.view.settings;

import dev.code_offline.basalt.model.settings.SettingsModel;

import java.util.EventListener;

public interface SettingsListener extends EventListener {
    void saveSettings(SettingsModel revertSettingsModel);
    void revertSettings(SettingsModel revertSettingsModel);
}
