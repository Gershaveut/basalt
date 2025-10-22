package dev.code_offline.basalt.view.tool.folder;

import dev.code_offline.basalt.model.Folder;

import java.util.EventListener;

public interface FolderListener extends EventListener {
    void openFile(long id);
    void newFile(Folder parent);
    void newFolder(Folder folder);
    void moveFile(long id, String path);
    void moveFolder(String is, String path);
    void rename(long id, String newName);
    void rename(String path, String newName);
    void delete(long id);
    void delete(String path);
}
