package org.gershaveut.basalt.view.tool.folder;

import org.gershaveut.basalt_share.model.Folder;
import org.jspecify.annotations.Nullable;

import java.util.EventListener;

public interface FolderListener extends EventListener {
    void openFile(long id);
    void newFile(@Nullable Folder parent);
    void newFolder(@Nullable Folder folder);
    void moveFile(long id, String path);
    void moveFolder(String id, String path);
    void author(long id, String author);
    void renameNote(long id, String newName);
    void renameFolder(String path, String newName);
    void deleteNote(long id);
    void deleteFolder(String path);
}
