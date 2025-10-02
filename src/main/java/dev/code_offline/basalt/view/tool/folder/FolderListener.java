package dev.code_offline.basalt.view.tool.folder;

import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;

import java.util.EventListener;

public interface FolderListener extends EventListener {
    void openFile(long id);
    void newFile(Folder parent);
    void newFolder(Folder folder);
    void rename(long id, String newName);
    void delete(Note note);
}
