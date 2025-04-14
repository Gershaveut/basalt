package dev.code_offline.basalt.controller;

import com.javadocking.dock.Position;
import com.javadocking.dock.TabDock;
import dev.code_offline.basalt.core.Util;
import dev.code_offline.basalt.model.Note;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.model.user.Role;
import dev.code_offline.basalt.model.user.User;
import dev.code_offline.basalt.view.tool.FolderPanel;
import dev.code_offline.basalt.view.tool.MarkdownEditorPanel;
import dev.code_offline.basalt.view.tool.Tool;
import dev.code_offline.basalt.view.tool.graph.GraphPanel;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class NoteController {
    public List<Note> notes = List.of(new Note("Test", new User("Test", 0, Role.MEMBER, null), "Test"));

    private final GraphPanel graphPanel;
    private final FolderPanel folderPanel;

    private final TabDock tabDock;

    private @Nullable Note selectedNote;

    public NoteController(GraphPanel graphPanel, FolderPanel folderPanel, TabDock tabDock) {
        this.graphPanel = graphPanel;
        this.folderPanel = folderPanel;
        this.tabDock = tabDock;

        Sync();

        folderPanel.getTree().addTreeSelectionListener(e -> selectNote((Note) ((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).getUserObject()));
        graphPanel.graphCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var focusNode = graphPanel.graphCanvas.getFocusatedNode();

                if (focusNode == null)
                    return;

                selectNote((Note) focusNode);
            }
        });
    }

    private void Sync() {
        folderPanel.setNotes(notes);
        graphPanel.graphCanvas.setGraph(new Graph(new ArrayList<>(notes)));
    }

    private void selectNote(Note note) {
        selectedNote = note;

        tabDock.addDockable(new Tool(new MarkdownEditorPanel(selectedNote)), new Position());
    }
}
