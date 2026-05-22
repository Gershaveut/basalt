package org.gershaveut.basalt.view.tool.file;

import org.gershaveut.basalt.model.file.SFile;
import org.jspecify.annotations.Nullable;

import java.util.EventListener;

public interface FilesListener extends EventListener {
    void openFile(long id);
    void newFile(@Nullable SFile parent, boolean isDirectory);
    void moveFile(long id, long toId);
    void author(long id, String author);
    void renameFile(long id, String newName);
    void deleteFile(long id);
}
