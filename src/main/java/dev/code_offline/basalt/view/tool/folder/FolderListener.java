package dev.code_offline.basalt.view.tool.folder;

import java.util.EventListener;

public interface FolderListener extends EventListener {
    void rename(long id, String newName);
}
