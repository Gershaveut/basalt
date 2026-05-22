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
import org.gershaveut.basalt.model.file.Note;
import org.gershaveut.basalt.model.file.SFile;
import org.gershaveut.basalt.model.graph.Graph;
import org.gershaveut.basalt.model.graph.Node;
import org.gershaveut.basalt.view.ApplicationFrame;
import org.gershaveut.basalt.view.menubar.MenuBar;
import org.gershaveut.basalt.view.menubar.MenuBarListener;
import org.gershaveut.basalt.view.start.StartFrame;
import org.gershaveut.basalt.view.tool.AbstractTool;
import org.gershaveut.basalt.view.tool.file.FileListener;
import org.gershaveut.basalt.view.tool.file.FileTool;
import org.gershaveut.basalt.view.tool.file.FilesListener;
import org.gershaveut.basalt.view.tool.file.FilesTool;
import org.gershaveut.basalt.view.tool.graph.GraphTool;
import org.gershaveut.basalt.view.tool.person.PersonProfileTool;
import org.gershaveut.basalt.view.tool.person.PersonsListener;
import org.gershaveut.basalt.view.tool.person.PersonsTool;
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

public class DatabaseController implements DatabaseListener, FilesListener, PersonsListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseController.class);
	
    public final Database database;
	
	private final StartFrame startFrame;
    private final ApplicationFrame applicationFrame;
    private final GraphTool graphTool;
    private final FilesTool filesTool;
    private final PersonsTool personsTool;
    private final StartController startController;
    private final MenuBar menuBar;

    private final TabDock tabDock;
    private final CompositeDock dock;

    private final String applicationFrameTitle;
    
    private final FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("Базальт проект (.zip)", "zip");
    private @Nullable Person clientPerson;

    public DatabaseController(ApplicationFrame applicationFrame, GraphTool graphTool, FilesTool filesTool, TabDock tabDock, CompositeDock dock, Database database, MenuBar menuBar, StartFrame startFrame, StartController startController, PersonsTool personsTool) {
        this.startFrame = startFrame;
        this.applicationFrame = applicationFrame;
        this.graphTool = graphTool;
        this.filesTool = filesTool;
        this.personsTool = personsTool;
        this.tabDock = tabDock;
		this.dock = dock;
		this.database = database;
		this.startController = startController;
        this.menuBar = menuBar;
		
        this.applicationFrameTitle = applicationFrame.getTitle();
		
        filesTool.addFilesListener(this);
        personsTool.addPersonsListener(this);

        graphTool.graphCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Node focusNode = graphTool.graphCanvas.getFocusatedNode();

                if (focusNode == null) return;

                openFile(focusNode.getId());
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
                        if (toolDockable instanceof FileTool editor) {
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
        database.getFiles().subscribe(files -> {
            var notes = files.stream().filter(file -> file.getExtension().equals(".md")).map(Note::new).toList();
          
            database.getClientPerson().subscribe(clientPerson -> {
                this.clientPerson = clientPerson;
                
                applicationFrame.setTitle(applicationFrameTitle + " - " + clientPerson.getRole());
                
                filesTool.setModel(files, clientPerson);
                database.getPersons().subscribe(persons -> {
                   personsTool.setModel(persons, clientPerson);
                });
                graphTool.graphCanvas.setGraph(new Graph(new ArrayList<>(notes)));
                
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
        TreePath treeNode = filesTool.getTree().getSelectionPath();

        if (treeNode != null) {
            var selected = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();

            if (selected instanceof Note note)
                openFile(note.getId());
        }
    }
    
    private void showErrorDialog(String message) {
        ApplicationUtil.showErrorDialog(applicationFrame, message);
    }
    
    @Override
    public void openFile(long id) {
        database.getFile(id).subscribe(file -> {
            database.getClientPerson().subscribe(clientPerson -> {
                database.readFile(file.getId()).subscribe(resource -> {
                    var fileTool = new FileTool(file, resource, applicationFrame, clientPerson);

                    fileTool.addFileListener(new FileListener() {
                        @Override
                        public void onSave(String text) {
                            database.writeFile(file.getId(), text.getBytes());
                        }

                        @Override
                        public void openProfile(long id) {
                            DatabaseController.this.openProfile(id);
                        }

                        @Override
                        public void openComments(long page) {
                            database.getComments(file.getId(), page).subscribe(commentPagedModel -> {
                                fileTool.setComments(commentPagedModel, page, longConsumerPair -> {
                                    database.getPerson(longConsumerPair.getFirst()).subscribe(person -> {
                                        longConsumerPair.getSecond().accept(person);
                                    });
                                });
                            });
                        }

                        @Override
                        public void addComment(String text, long totalPages) {
                            database.addComment(file.getId(), text, _ -> false, _ -> {
                                openComments(totalPages - 1);

                                return true;
                            });
                        }

                        @Override
                        public void editComment(long commentId, String text, long currentPage) {
                            database.editComment(file.getId(), commentId, text, _ -> false, _ -> {
                                openComments(currentPage);

                                return true;
                            });
                        }

                        @Override
                        public void deleteComment(long commentId, long currentPage) {
                            database.deleteComment(file.getId(), commentId, _ -> false, _ -> {
                                openComments(0); //TODO: оставлять на текущей странице

                                return true;
                            });
                        }
                    });

                    openDock(fileTool.getDockable());
                });
            });
        });
    }

    @Override
    public void newFile(@Nullable SFile parent, boolean isDirectory) {
        String path = "";
        
        if (parent != null)
            path = parent.getPath();
        
        var name = "Новый файл";
        
        if (isDirectory)
            name = "Новая папка";

        assert clientPerson != null;
        database.addFile(new SFile(name, path, clientPerson, isDirectory));
    }

    @Override
    public void moveFile(long id, long toId) {
        database.moveFile(id, toId, httpStatusCode -> {
            if (httpStatusCode == HttpStatus.CONFLICT) {
                showErrorDialog("Неправильное место размещения файла!");
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
            database.authorFile(id, Long.parseLong(author), onError);
        } catch (Exception ignored) {
            var person = database.getPerson(author).block();
            long peronId = -1;
            
            if (person != null)
                peronId = person.getId();
                
            database.authorFile(id, peronId, onError);
        }
    }
    
    @Override
    public void renameFile(long id, String newName) {
        database.renameFile(id, newName, httpStatusCode -> {
            if (httpStatusCode == HttpStatus.CONFLICT) {
                showErrorDialog("Имя файла уже занято!");
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
    public void deleteFile(long id) {
        ApplicationUtil.foreachNonList(tabDock::getDockableCount, tabDock::getDockable, (dockable) -> {
            if (dockable.getID().contains(String.valueOf(id)))
                tabDock.removeDockable(dockable);
        });
        
        database.deleteFile(id);
    }
}
