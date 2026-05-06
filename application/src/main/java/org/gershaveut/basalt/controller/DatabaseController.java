package org.gershaveut.basalt.controller;

import com.javadocking.DockingManager;
import com.javadocking.dock.CompositeDock;
import com.javadocking.dock.LeafDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.TabDock;
import com.javadocking.dockable.Dockable;
import org.gershaveut.basalt.ApplicationUtil;
import org.gershaveut.basalt.model.database.Database;
import org.gershaveut.basalt.model.database.DatabaseListener;
import org.gershaveut.basalt.model.graph.Graph;
import org.gershaveut.basalt.model.graph.Node;
import org.gershaveut.basalt.model.note.NoteInfo;
import org.gershaveut.basalt.model.note.NoteNode;
import org.gershaveut.basalt.view.ApplicationFrame;
import org.gershaveut.basalt.view.menubar.MenuBar;
import org.gershaveut.basalt.view.menubar.MenuBarListener;
import org.gershaveut.basalt.view.start.StartFrame;
import org.gershaveut.basalt.view.tool.AbstractTool;
import org.gershaveut.basalt.view.tool.folder.FolderListener;
import org.gershaveut.basalt.view.tool.folder.FolderTool;
import org.gershaveut.basalt.view.tool.graph.GraphTool;
import org.gershaveut.basalt.view.tool.note.NoteListener;
import org.gershaveut.basalt.view.tool.note.NoteTool;
import org.gershaveut.basalt.view.tool.person.PersonProfileTool;
import org.gershaveut.basalt.view.tool.person.PersonsListener;
import org.gershaveut.basalt.view.tool.person.PersonsTool;
import org.gershaveut.basalt_share.model.Folder;
import org.gershaveut.basalt_server.model.Note;
import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.function.Function;

public class DatabaseController implements DatabaseListener, FolderListener, PersonsListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseController.class);
	
    public final Database database;
	
	private final StartFrame startFrame;
    private final ApplicationFrame applicationFrame;
    private final GraphTool graphTool;
    private final FolderTool folderTool;
    private final PersonsTool personsTool;
    private final StartController startController;
    private final MenuBar menuBar;

    private final TabDock tabDock;
    private final CompositeDock dock;

    private final String applicationFrameTitle;
    
    private final FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("Базальт проект (.zip)", "zip");
    
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
        this.menuBar = menuBar;
		
        this.applicationFrameTitle = applicationFrame.getTitle();
		
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
                    if (dockable instanceof AbstractTool abstractTool) {
                        var toolDockable = abstractTool.getDockable();
                        if (toolDockable instanceof NoteTool editor) {
                            editor.save();
                        }
                    }
                });
            }

            @Override
            public void importProject() {
                var fileChooser = new JFileChooser();

                fileChooser.setFileFilter(zipFilter);

                var result = fileChooser.showOpenDialog(applicationFrame);

                if (result == JFileChooser.APPROVE_OPTION) {
                    database.importProject(fileChooser.getSelectedFile(), _ -> {
                        showErrorDialog("Ошибка при импортировании!");
                        return true;
                    });
                }
            }

            @Override
            public void exportProject() {
                var fileChooser = new JFileChooser();

                fileChooser.setFileFilter(zipFilter);

                var result = fileChooser.showSaveDialog(applicationFrame);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    var selectedFile = ApplicationUtil.ensureEndsWith(fileChooser.getSelectedFile().getPath(), ".zip");
                    
                    database.exportProject().subscribe(resource -> {
                        try {
                            var fileOutputStream = new FileOutputStream(selectedFile);
                            
                            fileOutputStream.write(resource.getContentAsByteArray());
                            
                            fileOutputStream.close();
                        } catch (Exception e) {
                            showErrorDialog("Ошибка при экспортировании!");
                        }
                    });
                }
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
    public synchronized void sync() {
        database.getNotes().subscribe(notes -> {
            var notesInfo = notes.stream().map(n -> new NoteInfo(n, database)).toList();
            var notesNode = notes.stream().map(n -> new NoteNode(n, database)).toList();
          
            database.getClientPerson().subscribe(clientPerson -> {
                applicationFrame.setTitle(applicationFrameTitle + " - " + clientPerson.getRole());
                
                database.getFolders().subscribe(folders -> {
                    folderTool.setModel(notesInfo, folders, clientPerson);
                });
                database.getPersons().subscribe(persons -> {
                   personsTool.setModel(persons, clientPerson);
                });
                graphTool.graphCanvas.setGraph(new Graph(new ArrayList<>(notesNode)));
                
                menuBar.updateAccess(clientPerson);
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
            database.getClientPerson().subscribe(clientPerson -> {
                database.getNoteText(note.getId()).subscribe(text -> {
                    var noteTool = new NoteTool(note, text, applicationFrame, clientPerson);

                    noteTool.addNoteListener(new NoteListener() {
                        @Override
                        public void onSave(String text) {
                            database.editNote(note.getId(), text);
                        }

                        @Override
                        public void openProfile(long id) {
                            DatabaseController.this.openProfile(id);
                        }

                        @Override
                        public void openComments(long page) {
                            database.getComments(note.getId(), page).subscribe(commentPagedModel -> {
                                noteTool.setComments(commentPagedModel, page, longConsumerPair -> {
                                    database.getPerson(longConsumerPair.getFirst()).subscribe(person -> {
                                        longConsumerPair.getSecond().accept(person);
                                    });
                                });
                            });
                        }

                        @Override
                        public void addComment(String text, long totalPages) {
                            database.addComment(note.getId(), text, _ -> false, _ -> {
                                openComments(totalPages - 1);

                                return true;
                            });
                        }

                        @Override
                        public void editComment(long commentId, String text, long currentPage) {
                            database.editComment(note.getId(), commentId, text, _ -> false, _ -> {
                                openComments(currentPage);

                                return true;
                            });
                        }

                        @Override
                        public void deleteComment(long commentId, long currentPage) {
                            database.deleteComment(note.getId(), commentId, _ -> false, _ -> {
                                openComments(0); //TODO: оставлять на текущей странице
                                
                                return true;
                            });
                        }
                    });

                    openDock(noteTool.getDockable());
                });
            });
        });
    }
    
    private void showErrorDialog(String message) {
        ApplicationUtil.showErrorDialog(applicationFrame, message);
    }
    
    @Override
    public void openFile(long id) {
        openNote(id);
    }

    @Override
    public void newFile(@Nullable Folder parent) {
        String path = null;
        
        if (parent != null)
            path = parent.getPath();
            
        database.addNote(new Note("Новая записка", path));
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
        
        database.moveFolder(id, path, httpStatusCode -> {
            if (httpStatusCode == HttpStatus.CONFLICT) {
                showErrorDialog("Неправильное место размещения папки!");
                return true;
            }
            
            return false;
        });
    }
    
    @Override
    public void author(long id, String author) {
        Function<HttpStatusCode, Boolean> onError = (httpStatusCode) -> {
            if (httpStatusCode == HttpStatus.NOT_FOUND) {
                showErrorDialog("Пользователь не найден!");
                
                return true;
            }
            
            return false;
        };
        
        try {
            database.authorNote(id, Long.parseLong(author), onError);
        } catch (Exception ignored) {
            var person = database.getPerson(author).block();
            long peronId = -1;
            
            if (person != null)
                peronId = person.getId();
                
            database.authorNote(id, peronId, onError);
        }
    }
    
    @Override
    public void renameNote(long id, String newName) {
        database.renameNote(id, newName, httpStatusCode -> {
            if (httpStatusCode == HttpStatus.CONFLICT) {
                showErrorDialog("Имя записки уже занято!");
                return true;
            }
            
            return false;
		});
    }
   
    @Override
    public void renameFolder(String path, String newName) {
        database.renameFolder(path, newName, httpStatusCode -> {
            if (httpStatusCode == HttpStatus.CONFLICT) {
                showErrorDialog("Имя папки уже занято!");
                return true;
            }
            
            return false;
        });
    }
    
    @Override
    public void createPerson(Person person) {
        database.addPerson(person, httpStatusCode -> {
            if (httpStatusCode == HttpStatus.CONFLICT) {
                showErrorDialog("Пользователь с таким именем уже существует!");
                
                return true;
            }
            
            return false;
        });
    }

    @Override
    public void openProfile(long id) {
        database.getPerson(id).subscribe(person -> {
            var personProfile = new PersonProfileTool(person);

            openDock(personProfile.getDockable());
        });
    }
    
    private void openDock(Dockable dockable) {
        if (tabDock.isEmpty())
            dock.addChildDock(tabDock, new Position(Position.CENTER));
        
        tabDock.addDockable(dockable, new Position());
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
