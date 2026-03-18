package org.gershaveut.basalt.view.tool.markdown;

import java.util.EventListener;

public interface MarkdownListener extends EventListener {
    void onSave(String text);
}
