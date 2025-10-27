package dev.code_offline.basalt.controller;

import com.javadocking.dock.CompositeDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.TabDock;
import dev.code_offline.basalt.core.Util;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.model.database.DatabaseListener;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.note.NoteInfo;
import dev.code_offline.basalt.model.note.NoteNode;
import dev.code_offline.basalt.view.BasaltFrame;
import dev.code_offline.basalt.view.menubar.MenuBar;
import dev.code_offline.basalt.view.menubar.MenuBarListener;
import dev.code_offline.basalt.view.start.StartFrame;
import dev.code_offline.basalt.view.tool.Tool;
import dev.code_offline.basalt.view.tool.folder.FolderListener;
import dev.code_offline.basalt.view.tool.folder.FolderPanel;
import dev.code_offline.basalt.view.tool.graph.GraphPanel;
import dev.code_offline.basalt.view.tool.markdown.MarkdownEditorPanel;
import org.springframework.lang.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class DatabaseController implements DatabaseListener, FolderListener {
	public final Database database;
	
	private final StartFrame startFrame;
    private final BasaltFrame basaltFrame;
    private final GraphPanel graphPanel;
    private final FolderPanel folderPanel;
    private final StartController startController;

    private final TabDock tabDock;
    private final CompositeDock dock;

    public DatabaseController(BasaltFrame basaltFrame, GraphPanel graphPanel, FolderPanel folderPanel, TabDock tabDock, CompositeDock dock, Database database, MenuBar menuBar, StartFrame startFrame, StartController startController) {
        this.startFrame = startFrame;
        this.basaltFrame = basaltFrame;
        this.graphPanel = graphPanel;
        this.folderPanel = folderPanel;
        this.tabDock = tabDock;
		this.dock = dock;
		this.database = database;
		this.startController = startController;
		
		var tree = folderPanel.getTree();

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    openSelectedNote();
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
                Node focusNode = graphPanel.graphCanvas.getFocusatedNode();

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

        database.addDatabaseListener(this);
        
        basaltFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close(true);
            }
        });
    }
    
    private void close(boolean exit) {
        graphPanel.graphCanvas.dispose();
        basaltFrame.dispose();
        
        database.close();
        
        if (startController.getContext() != null)
            startController.getContext().close();
        
        startFrame.setVisible(!exit);
        
        if (exit)
            System.exit(0);
    }
    
    private void close() {
        close(false);
    }
    
    @Override
    public void sync() {
        database.getNotes().subscribe(notes -> {
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
            
            var notesInfo = notes.stream().map(n -> new NoteInfo(n, database)).toList();
            var notesNode = notes.stream().map(n -> new NoteNode(n, database)).toList();
           
            database.getFolders().subscribe(folders -> {
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
        TreePath treeNode = folderPanel.getTree().getSelectionPath();

        if (treeNode != null) {
            var selected = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();

            if (selected instanceof NoteInfo note)
                openNote(note.getId());
        }
    }

    private void openNote(long id) {
        database.getNote(id).subscribe(note -> {
            var markdownEditor = new MarkdownEditorPanel(note, basaltFrame);

            markdownEditor.addMarkdownListener(text -> database.editNote(note.getId(), text));

            var addToDock = tabDock.isEmpty();
        
            tabDock.addDockable(new Tool(markdownEditor), new Position());
        
            if (addToDock)
                dock.addChildDock(tabDock, new Position(Position.CENTER));
        });
    }

    private void newFileCreate(@Nullable Folder folder) {
        String path = null;
        
        if (folder != null)
            path = folder.getPath();
        
        database.addNote(new Note("Новая записка", 1, path)); // TODO: получение пользователя клиента
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

        database.addFolder(newFolder);
    }
    
    @Override
    public void moveFile(long id, String path) {
       database.moveNote(id, path);
    }
    
    @Override
    public void moveFolder(String id, String path) {
        database.moveFolder(id, path);
    }
    
    @Override
    public void rename(long id, String newName) {
        database.renameNote(id, newName);
    }

    @Override
    public void rename(String path, String newName) {
        database.renameFolder(path, newName);
    }

    @Override
    public void delete(long id) {
        Util.foreachNonList(tabDock::getDockableCount, tabDock::getDockable, (dockable) -> {
            if (dockable.getID().contains(String.valueOf(id)))
                tabDock.removeDockable(dockable);
        });
        
        database.deleteNote(id);
    }

    @Override
    public void delete(String path) {
        database.deleteFolder(path);
    }
}
