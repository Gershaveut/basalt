package dev.code_offline.basalt.view.menubar;

import java.util.EventListener;

public interface MenuBarListener extends EventListener {
    void newFile();
    void closeProject();
    void save();
}
