package org.gershaveut.basalt.model.settings;

import java.util.EventListener;

public interface SettingListener extends EventListener {
    void valueChanged(Object value);
}
