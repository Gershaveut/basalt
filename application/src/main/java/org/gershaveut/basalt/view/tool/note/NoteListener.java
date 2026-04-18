package org.gershaveut.basalt.view.tool.note;

import java.util.EventListener;

public interface NoteListener extends EventListener {
    void onSave(String text);
}
