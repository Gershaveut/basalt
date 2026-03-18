package org.gershaveut.basalt.view.settings;

import org.gershaveut.basalt.model.settings.SettingsModel;

import java.util.EventListener;

public interface SettingsListener extends EventListener {
    void saveSettings(SettingsModel revertSettingsModel);
    void revertSettings(SettingsModel revertSettingsModel);
    void setValue(String name, Object value);
}
