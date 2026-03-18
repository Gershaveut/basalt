package org.gershaveut.basalt.view.menubar;

import java.util.EventListener;

public interface MenuBarListener extends EventListener {
    void closeProject();
    void save();
    void importProject();
    void exportProject();
    void exit();
}
