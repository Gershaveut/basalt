package dev.code_offline.basalt.controller;

import com.javadocking.dock.CompositeDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.TabDock;
import dev.code_offline.basalt.controller.client.Client;
import dev.code_offline.basalt.controller.client.ClientListener;
import dev.code_offline.basalt.core.Util;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.note.NoteInfo;
import dev.code_offline.basalt.model.note.NoteNode;
import dev.code_offline.basalt.view.BasaltFrame;
import dev.code_offline.basalt.view.StartFrame;
import dev.code_offline.basalt.view.menubar.MenuBar;
import dev.code_offline.basalt.view.menubar.MenuBarListener;
import dev.code_offline.basalt.view.tool.Tool;
import dev.code_offline.basalt.view.tool.folder.FolderListener;
import dev.code_offline.basalt.view.tool.folder.FolderPanel;
import dev.code_offline.basalt.view.tool.graph.GraphPanel;
import dev.code_offline.basalt.view.tool.markdown.MarkdownEditorPanel;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ClientController implements ClientListener, FolderListener {
	public Client client;
    
    private final StartFrame startFrame;
    private final BasaltFrame basaltFrame;
    private final GraphPanel graphPanel;
    private final FolderPanel folderPanel;

    private final TabDock tabDock;
    private final CompositeDock dock;

    public ClientController(BasaltFrame basaltFrame, GraphPanel graphPanel, FolderPanel folderPanel, TabDock tabDock, CompositeDock dock, Client client, MenuBar menuBar, StartFrame startFrame) {
        this.startFrame = startFrame;
        this.basaltFrame = basaltFrame;
        this.graphPanel = graphPanel;
        this.folderPanel = folderPanel;
        this.tabDock = tabDock;
		this.dock = dock;
		this.client = client;

        var tree = folderPanel.getTree();

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
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

        menuBar.addMenuBarListener(new MenuBarListener() {
            @Override
            public void newFile() {
                newFileCreate(null);
            }
            
            @Override
            public void closeProject() {
                close();
            }
            
            @Override
            public void save() {
                Util.foreachNonList(tabDock::getDockableCount, tabDock::getDockable, (dockable) -> {
                    if (((Tool) dockable).getBasaltDockable() instanceof MarkdownEditorPanel markdownEditorPanel)
                        markdownEditorPanel.save();
                });
            }
            
            @Override
            public void exit() {
                close(true);
            }
        });

        client.addClientListener(this);
        
        basaltFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close(true);
            }
        });
    }
    
    private void close(boolean exit) {
        basaltFrame.dispose();
        
        client.close();
        
        if (startFrame.context != null)
            startFrame.context.close();
        
        startFrame.setVisible(!exit);
        
        if (exit)
            System.exit(0);
    }
    
    private void close() {
        close(false);
    }
    
    @Override
    public void sync() {
        client.getNotes().subscribe(notes -> {
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
                        
                        notes.stream().filter(n -> n.getName().matches(name)).forEach(findNote -> {
                            var number = findNote.getId();
                            
                            if (number != note.getId() && links.stream().noneMatch(l -> l == number))
                                links.add(number);
                        });
                    } catch (Exception ignored){
                    }
                }
                
                note.setLinks(links);
            });
            
            var notesInfo = notes.stream().map(n -> new NoteInfo(n, client)).toList();
            var notesNode = notes.stream().map(n -> new NoteNode(n, client)).toList();
           
            client.getFolders().subscribe(folders -> {
                folderPanel.setModel(notesInfo, folders);
            });
            graphPanel.graphCanvas.setGraph(new Graph(new ArrayList<>(notesNode)));
        });
    }
    
    @Override
    public void onLostConnection() {
        JOptionPane.showMessageDialog(basaltFrame, "Соединение потеряно", "Ошибка соединения", JOptionPane.ERROR_MESSAGE);
        close();
    }
    
    private void openSelectedNote() {
        @Nullable TreePath treeNode = folderPanel.getTree().getSelectionPath();

        if (treeNode != null) {
            var selected = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();

            if (selected instanceof NoteInfo note)
                openNote(note.getId());
        }
    }

    private void openNote(long id) {
        client.getNote(id).subscribe(note -> {
            var markdownEditor = new MarkdownEditorPanel(note, basaltFrame);

            markdownEditor.addMarkdownListener(text -> client.editNote(note.getId(), text));

            var addToDock = tabDock.isEmpty();
        
            tabDock.addDockable(new Tool(markdownEditor), new Position());
        
            if (addToDock)
                dock.addChildDock(tabDock, new Position(Position.CENTER));
        });
    }

    private void newFileCreate(@Nullable Folder folder) {
        @Nullable String path = null;
        
        if (folder != null)
            path = folder.getPath();
        
        client.addNote(new Note("Новая записка", client.getClientPerson().getId(), path));
    }

    @Override
    public void openFile(long id) {
        openNote(id);
    }

    @Override
    public void newFile(@Nullable Folder parent) {
        newFileCreate(parent);
    }

    @Override
    public void newFolder(@Nullable Folder parent) {
        var newFolder = new Folder("Новая папка", parent);

        client.addFolder(newFolder);
    }
    
    @Override
    public void moveFile(long id, String path) {
       client.moveNote(id, path);
    }
    
    @Override
    public void moveFolder(String id, String path) {
        client.moveFolder(id, path);
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
        Util.foreachNonList(tabDock::getDockableCount, tabDock::getDockable, (dockable) -> {
            if (dockable.getID().contains(String.valueOf(id)))
                tabDock.removeDockable(dockable);
        });
        
        client.deleteNote(id);
    }

    @Override
    public void delete(String path) {
        client.deleteFolder(path);
    }
}
