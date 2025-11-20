package dev.code_offline.basalt.controller;

import com.javadocking.DockingManager;
import com.javadocking.dock.CompositeDock;
import com.javadocking.dock.LeafDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.TabDock;
import com.javadocking.dockable.Dockable;
import dev.code_offline.basalt.ApplicationUtil;
import dev.code_offline.basalt_share.model.Folder;
import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.model.database.DatabaseListener;
import dev.code_offline.basalt.model.graph.Graph;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt_share.model.Note;
import dev.code_offline.basalt.model.note.NoteInfo;
import dev.code_offline.basalt.model.note.NoteNode;
import dev.code_offline.basalt_share.model.Person;
import dev.code_offline.basalt_share.model.Role;
import dev.code_offline.basalt.view.ApplicationFrame;
import dev.code_offline.basalt.view.menubar.MenuBar;
import dev.code_offline.basalt.view.menubar.MenuBarListener;
import dev.code_offline.basalt.view.start.StartFrame;
import dev.code_offline.basalt.view.tool.AbstractTool;
import dev.code_offline.basalt.view.tool.folder.FolderListener;
import dev.code_offline.basalt.view.tool.folder.FolderTool;
import dev.code_offline.basalt.view.tool.graph.GraphTool;
import dev.code_offline.basalt.view.tool.markdown.MarkdownEditorTool;
import dev.code_offline.basalt.view.tool.person.PersonProfileTool;
import dev.code_offline.basalt.view.tool.person.PersonsListener;
import dev.code_offline.basalt.view.tool.person.PersonsTool;
import org.springframework.lang.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class DatabaseController implements DatabaseListener, FolderListener, PersonsListener {
	public final Database database;
	
	private final StartFrame startFrame;
    private final ApplicationFrame applicationFrame;
    private final GraphTool graphTool;
    private final FolderTool folderTool;
    private final PersonsTool personsTool;
    private final StartController startController;

    private final TabDock tabDock;
    private final CompositeDock dock;

    private final String basaltFrameTitle;
    
    public DatabaseController(ApplicationFrame applicationFrame, GraphTool graphTool, FolderTool folderTool, TabDock tabDock, CompositeDock dock, Database database, MenuBar menuBar, StartFrame startFrame, StartController startController, PersonsTool personsTool) {
        this.startFrame = startFrame;
        this.applicationFrame = applicationFrame;
        this.graphTool = graphTool;
        this.folderTool = folderTool;
        this.personsTool = personsTool;
        this.tabDock = tabDock;
		this.dock = dock;
		this.database = database;
		this.startController = startController;
		
        this.basaltFrameTitle = applicationFrame.getTitle();
		
		var tree = folderTool.getTree();

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

        folderTool.addFolderListener(this);
        personsTool.addPersonsListener(this);

        graphTool.graphCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Node focusNode = graphTool.graphCanvas.getFocusatedNode();

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
            
            @SuppressWarnings("unchecked")
			@Override
            public void save() {
                var dockModel = DockingManager.getDockModel();
                var dockables = new ArrayList<Dockable>();
                
                dockModel.getRootKeys(applicationFrame).forEachRemaining(o -> {
                    String key = (String) o;
                    var dock = dockModel.getRootDock(key);
                    
                    if (dock instanceof CompositeDock compositeDock) {
                        ApplicationUtil.foreachNonList(compositeDock::getChildDockCount, compositeDock::getChildDock, (childDock) -> {
                            if (childDock instanceof LeafDock leafDock) {
                                ApplicationUtil.foreachNonList(leafDock::getDockableCount, leafDock::getDockable, dockables::add);
                            } else if (childDock instanceof CompositeDock compositeDock1) {
                                ApplicationUtil.foreachNonList(compositeDock1::getChildDockCount, compositeDock1::getChildDock, (childDock1) -> {
                                    if (childDock1 instanceof LeafDock leafDock) {
                                        ApplicationUtil.foreachNonList(leafDock::getDockableCount, leafDock::getDockable, dockables::add);
                                    }
                                });
                            }
                        });
                    }
                });
                
                dockables.forEach(dockable -> {
                    if (((AbstractTool) dockable).getDockable() instanceof MarkdownEditorTool markdownEditorTool)
                        markdownEditorTool.save();
                });
            }
            
            @Override
            public void exit() {
                close(true);
            }
        });

        database.addDatabaseListener(this);
        
        applicationFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close(true);
            }
        });
    }
    
    private void close(boolean exit) {
        graphTool.graphCanvas.dispose();
        applicationFrame.dispose();
        
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
            var notesInfo = notes.stream().map(n -> new NoteInfo(n, database)).toList();
            var notesNode = notes.stream().map(n -> new NoteNode(n, database)).toList();
          
            database.getClientPerson().subscribe(clientPerson -> {
                applicationFrame.setTitle(basaltFrameTitle + " - " + clientPerson.getRole().name);
                
                database.getFolders().subscribe(folders -> {
                    folderTool.setModel(notesInfo, folders, clientPerson);
                });
                database.getPersons().subscribe(persons -> {
                   personsTool.setModel(persons, clientPerson);
                });
                graphTool.graphCanvas.setGraph(new Graph(new ArrayList<>(notesNode)));
            });
        });
    }
    
    @Override
    public void onLostConnection() {
        JOptionPane.showMessageDialog(applicationFrame, "Соединение потеряно", "Ошибка соединения", JOptionPane.ERROR_MESSAGE);
        close();
    }
    
    private void openSelectedNote() {
        TreePath treeNode = folderTool.getTree().getSelectionPath();

        if (treeNode != null) {
            var selected = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();

            if (selected instanceof NoteInfo note)
                openNote(note.getId());
        }
    }

    private void openNote(long id) {
        database.getNote(id).subscribe(note -> {
            database.getClientPerson().subscribe(person -> {
                var markdownEditor = new MarkdownEditorTool(note, applicationFrame, person);
                
                markdownEditor.addMarkdownListener(text -> database.editNote(note.getId(), text));
                
                var addToDock = tabDock.isEmpty();
                
                tabDock.addDockable(markdownEditor.getDockable(), new Position());
                
                if (addToDock)
                    dock.addChildDock(tabDock, new Position(Position.CENTER));
            });
        });
    }

    private void newFileCreate(@Nullable Folder folder) {
        String path = null;
        
        if (folder != null)
            path = folder.getPath();
        
        database.addNote(new Note("Новая записка", path));
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
        if (id.equals(path))
            return;
        
        database.moveFolder(id, path);
    }
    
    @Override
    public void author(long id, String author) {
        try {
            database.authorNote(id, Long.parseLong(author));
        } catch (Exception ignored) {
            database.getPerson(author).subscribe(person -> {
                database.authorNote(id, person.getId());
            });
        }
    }
    
    @Override
    public void renameNote(long id, String newName) {
        database.renameNote(id, newName);
    }

    @Override
    public void renameFolder(String path, String newName) {
        database.renameFolder(path, newName);
    }
    
    @Override
    public void createPerson(Person person) {
        database.addPerson(person);
    }
    
    @Override
    public void openProfile(long id) {
        TreePath treeNode = personsTool.getTree().getSelectionPath();
        
        if (treeNode != null) {
            var selected = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();
            
            if (selected instanceof Person person) {
                database.getPerson(person.getId()).subscribe(person1 -> {
                    var personProfile = new PersonProfileTool(person1);
                
                    var addToDock = tabDock.isEmpty();
                
                    tabDock.addDockable(personProfile.getDockable(), new Position());
                
                    if (addToDock)
                        dock.addChildDock(tabDock, new Position(Position.CENTER));
                });
            }
        }
    }
    
    @Override
    public void rolePerson(long id, Role role) {
        database.rolePerson(id, role);
    }
    
    @Override
    public void deletePerson(long id, boolean deleteNotes) {
        database.deletePerson(id, deleteNotes);
    }
    
    @Override
    public void deleteNote(long id) {
        ApplicationUtil.foreachNonList(tabDock::getDockableCount, tabDock::getDockable, (dockable) -> {
            if (dockable.getID().contains(String.valueOf(id)))
                tabDock.removeDockable(dockable);
        });
        
        database.deleteNote(id);
    }

    @Override
    public void deleteFolder(String path) {
        database.deleteFolder(path);
    }
}
