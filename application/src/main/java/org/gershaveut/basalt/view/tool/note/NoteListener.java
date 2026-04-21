package org.gershaveut.basalt.view.tool.note;

import java.util.EventListener;

public interface NoteListener extends EventListener {
    void onSave(String text);
    void openProfile(long id);
    void openComments(long page);
    void addComment(String text, long totalPages);
}
