package org.gershaveut.basalt.view.tool.file;

import java.util.EventListener;

public interface FileListener extends EventListener {
    void onSave(String text);
    void openProfile(long id);
    void openComments(long page);
    void addComment(String text, long totalPages);
    void editComment(long commentId, String text, long currentPage);
    void deleteComment(long commentId, long currentPage);
}
