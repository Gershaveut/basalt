package dev.code_offline.basalt.controller;

import com.javadocking.dock.CompositeDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.TabDock;
import dev.code_offline.basalt.core.client.Client;
import dev.code_offline.basalt.core.client.ClientListener;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.note.NoteNode;
import dev.code_offline.basalt.view.MainFrame;
import dev.code_offline.basalt.view.menubar.MenuBar;
import dev.code_offline.basalt.view.menubar.MenuBarAdapter;
import dev.code_offline.basalt.view.tool.Tool;
import dev.code_offline.basalt.view.tool.folder.FolderListener;
import dev.code_offline.basalt.view.tool.folder.FolderPanel;
import dev.code_offline.basalt.view.tool.graph.GraphPanel;
import dev.code_offline.basalt.view.tool.markdown.MarkdownEditorPanel;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class NoteController implements ClientListener, FolderListener {
	public Client client;
    
    private final MainFrame mainFrame;
    private final GraphPanel graphPanel;
    private final FolderPanel folderPanel;

    private final TabDock tabDock;
    private final CompositeDock dock;

    public NoteController(MainFrame mainFrame, GraphPanel graphPanel, FolderPanel folderPanel, TabDock tabDock, CompositeDock dock, Client client, MenuBar menuBar) {
        this.mainFrame = mainFrame;
        this.graphPanel = graphPanel;
        this.folderPanel = folderPanel;
        this.tabDock = tabDock;
		this.dock = dock;
		this.client = client;

        var tree = folderPanel.getTree();

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedNote();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    @Nullable TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    tree.setSelectionPath(selPath);

                    if (selRow > -1) {
                        tree.setSelectionRow(selRow);
                    }
                }
            }
        });
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openSelectedNote();
                }
            }
        });

        folderPanel.addFolderListener(this);

        graphPanel.graphCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                @Nullable Node focusNode = graphPanel.graphCanvas.getFocusatedNode();

                if (focusNode == null) return;

                openNote((focusNode.getId()));
            }
        });

        sync();

        menuBar.addMenuBarListener(new MenuBarAdapter() {
            @Override
            public void newFile() {
                newFileCreate(client.getRoot());
            }

            @Override
            public void save() {
                // TODO: Сохранение по нажатию кнопки в меню
            }
        });

        client.addDatabaseListener(this);
    }

    @Override
    public void sync() {
        var notes = client.getNotes();

        notes.forEach(note -> {
            var links = new ArrayList<Long>();

            var patternId = Pattern.compile("\\[(\\d*?)]");
            var patternName = Pattern.compile("\\[\\[(.*?)]]");

            var matcherId = patternId.matcher(note.getText());
            var matcherName = patternName.matcher(note.getText());

            while (matcherId.find()) {
                try {
                    var number = Long.parseLong(matcherId.group(1).trim());

                    if (number != note.getId() && links.stream().noneMatch(l -> l == number))
                        links.add(number);
                } catch (Exception ignored) {
                }
            }

            while (matcherName.find()) {
                try {
                    var name = matcherName.group(1).trim();

                    var number = notes.stream().filter(n -> n.getName().matches(name)).findFirst().orElseThrow().getId();

                    if (number != note.getId() && links.stream().noneMatch(l -> l == number))
                        links.add(number);
                } catch (Exception ignored){
                }
            }

            note.setLinks(links);
        });

        folderPanel.setModel(notes, client.getFolders(), client.getRoot());
        graphPanel.graphCanvas.setGraph(new Graph(new ArrayList<>(notes.stream().map(n -> new NoteNode(n, client)).toList())));
    }

    private void openSelectedNote() {
        @Nullable TreePath treeNode = folderPanel.getTree().getSelectionPath();

        if (treeNode != null) {
            var selected = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();

            if (selected instanceof Note note)
                openNote(note.getId());
        }
    }

    private void openNote(long id) {
        var note = client.getNote(id);

        var markdownEditor = new MarkdownEditorPanel(note, mainFrame);

        markdownEditor.addMarkdownListener(text -> client.editNote(note.getId(), text));

        var addToDock = tabDock.isEmpty();
        
        tabDock.addDockable(new Tool(markdownEditor), new Position());
        
        if (addToDock)
            dock.addChildDock(tabDock, new Position(Position.CENTER));
    }

    private void newFileCreate(Folder folder) {
        client.addNote(new Note("Новая записка", client.getClientPerson().getId(), folder));
    }

    @Override
    public void openFile(long id) {
        openNote(id);
    }

    @Override
    public void newFile(Folder parent) {
        newFileCreate(parent);
    }

    @Override
    public void newFolder(Folder parent) {
        var newFolder = new Folder("Новая папка", parent);

        int c = 0;

        while (true) {
            if (client.getFolders().stream().noneMatch(folder -> folder.hashCode() == newFolder.hashCode())) {
                break;
            } else {
                ++c;
                newFolder.setName("Новая папка " + c);
            }
        }

        client.addFolder(newFolder);
    }

    @Override
    public void rename(long id, String newName) {
        client.renameNote(id, newName);
    }

    @Override
    public void rename(String path, String newName) {
        client.renameFolder(path, newName);
    }

    @Override
    public void delete(long id) {
        client.deleteNote(id);
    }

    @Override
    public void delete(String path) {
        client.deleteFolder(path);
    }
}
