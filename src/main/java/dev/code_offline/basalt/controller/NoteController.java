package dev.code_offline.basalt.controller;

import com.javadocking.dock.Position;
import com.javadocking.dock.TabDock;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.client.Client;
import dev.code_offline.basalt.model.client.ClientListener;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.note.NoteNode;
import dev.code_offline.basalt.view.menubar.MenuBar;
import dev.code_offline.basalt.view.menubar.MenuBarAdapter;
import dev.code_offline.basalt.view.tool.FolderPanel;
import dev.code_offline.basalt.view.tool.MarkdownEditorPanel;
import dev.code_offline.basalt.view.tool.Tool;
import dev.code_offline.basalt.view.tool.graph.GraphPanel;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class NoteController implements ClientListener {
    public Client client;

    private final GraphPanel graphPanel;
    private final FolderPanel folderPanel;
    private final MenuBar menuBar;

    private final TabDock tabDock;

    private @Nullable Note selectedNote;
    private @Nullable MarkdownEditorPanel selectedNoteEditorPanel;

    public NoteController(GraphPanel graphPanel, FolderPanel folderPanel, TabDock tabDock, Client client, MenuBar menuBar) {
        this.graphPanel = graphPanel;
        this.folderPanel = folderPanel;
        this.tabDock = tabDock;
        this.client = client;
        this.menuBar = menuBar;

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

        sync();

        menuBar.addMenuBarListener(new MenuBarAdapter() {
            @Override
            public void newFile() {
                client.addNote(new Note("Новая записка", client.getClientPerson().getId()));
            }

            @Override
            public void save() {
                if (selectedNote != null && selectedNoteEditorPanel != null) {
                    client.editNote(selectedNote.getId(), selectedNoteEditorPanel.getText());
                }
            }
        });

        client.addDatabaseListener(this);
    }

    @Override
    public void sync() {
        var notes = client.getNotes();

        folderPanel.setNotes(notes);
        graphPanel.graphCanvas.setGraph(new Graph(new ArrayList<>(notes.stream().map(NoteNode::new).toList())));
    }

    private void selectNote(Note note) {
        selectedNote = note;
        selectedNoteEditorPanel = new MarkdownEditorPanel(selectedNote);

        tabDock.addDockable(new Tool(selectedNoteEditorPanel), new Position());
    }
}
