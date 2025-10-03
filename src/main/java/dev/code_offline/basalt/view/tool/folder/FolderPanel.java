package dev.code_offline.basalt.view.tool.folder;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.view.input.InputListener;
import dev.code_offline.basalt.view.input.InputTextFrame;
import dev.code_offline.basalt.view.tool.BasaltDockable;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class FolderPanel extends JPanel implements BasaltDockable {
	private final EventListenerList listeners = new EventListenerList();
	
	private final JTree tree = new JTree(new Object[0]);
	private final JPopupMenu popupMenu = new JPopupMenu();
	
	private @Nullable Object selectedNode;
	private @Nullable Object selectedParentNode;
	
	public FolderPanel() {
		super(new BorderLayout());
		
		// tree.setDragEnabled(true);
		// tree.setDropMode(DropMode.ON_OR_INSERT);
		// tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		tree.setCellRenderer(new FolderTreeCellRenderer());
		
		var newFile = new JMenuItem("Новый файл");
		var newFolder = new JMenuItem("Новая папка");
		
		var openFile = new JMenuItem("Открыть файл");
		
		var rename = new JMenuItem("Переименовать");
		var delete = new JMenuItem("Удалить");
		
		openFile.addActionListener(e -> {
			assert selectedNode != null;
			var selectedNote = (Note) selectedNode;
			
			for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
				listener.openFile(selectedNote.getId());
			}
		});
		newFile.addActionListener(e -> {
			@Nullable Object selected = selectedNode;
			Folder folder = (Folder) ((DefaultMutableTreeNode) tree.getModel().getRoot()).getUserObject();
			
			if (selected != null) {
				if (selected instanceof Folder f) {
					folder = f;
				} else {
					assert selectedParentNode != null;
					folder = (Folder) selectedParentNode;
				}
			}
			
			for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
				listener.newFile(folder);
			}
		});
		newFolder.addActionListener(e -> {
			assert selectedNode != null;
			var selected = selectedNode;
			Folder folder;
			
			if (selected instanceof Folder f) {
				folder = f;
			} else {
				assert selectedParentNode != null;
				folder = (Folder) selectedParentNode;
			}
			
			for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
				listener.newFolder(folder);
			}
		});
		rename.addActionListener(e -> {
			assert selectedNode != null;
            String name;

            if (selectedNode instanceof Note note) {
                name = note.getName();
            } else {
                name = ((Folder) selectedNode).getName();
            }

			var input = new InputTextFrame("Переименовать", "Переименовать - " + name, name);
			input.addInputListener(new InputListener() {
                @Override
                public void confirm(Object value) {
                    for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
                        if (selectedNode instanceof Note note) {
                            listener.rename(note.getId(), value.toString());
                        } else {
                            var folder = (Folder) selectedNode;

                            listener.rename(folder.getPath(), value.toString());
                        }
                    }
                }

                @Override
                public void cancel() {

                }
            });
			input.setVisible(true);
		});
		delete.addActionListener(e -> {
			assert selectedNode != null;

            for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
                if (selectedNode instanceof Note note) {
                    listener.delete(note.getId());
                } else {
                    listener.delete(((Folder) selectedNode).getPath());
                }
            }
		});
		
		var separator1 = new JSeparator();
		var separator2 = new JSeparator();
		
		popupMenu.add(newFile);
		popupMenu.add(newFolder);
		popupMenu.add(separator1);
		popupMenu.add(openFile);
		popupMenu.add(separator2);
		popupMenu.add(rename);
		popupMenu.add(delete);
		
		Consumer<PopupMenuContext> setPopupMenuContext = (context) -> {
            openFile.setVisible(false);
            rename.setVisible(false);
            delete.setVisible(false);
            separator1.setVisible(false);
            separator2.setVisible(false);

            switch (context) {
                case PopupMenuContext.Note -> {
                    openFile.setVisible(true);
                    rename.setVisible(true);
                    delete.setVisible(true);
                    separator1.setVisible(true);
                    separator2.setVisible(true);
                }
                case PopupMenuContext.Folder -> {
                    rename.setVisible(true);
                    delete.setVisible(true);
                    separator2.setVisible(true);
                }
                default -> {
                }
            }
		};
	
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					showPopupMenu(setPopupMenuContext, e.getX(), e.getY());
				}
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
					showPopupMenu(setPopupMenuContext, 0, 0); // TODO: Назначить координату y
				}
			}
		});
		
		add(tree, BorderLayout.CENTER);
	}
	
	private void showPopupMenu(Consumer<PopupMenuContext> setPopupMenuContext, int x, int y) {
		@Nullable TreePath treeNode = getTree().getSelectionPath();
        var context = PopupMenuContext.Empty;

		if (treeNode != null) {
			selectedNode = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();
			selectedParentNode = ((DefaultMutableTreeNode) treeNode.getParentPath().getLastPathComponent()).getUserObject();

            if (selectedNode instanceof Note) {
                context = PopupMenuContext.Note;
            } else if (selectedNode instanceof Folder) {
                context = PopupMenuContext.Folder;
            }
		}

        setPopupMenuContext.accept(context);
		
		popupMenu.show(tree, x, y);
	}
	
	public void addFolderListener(FolderListener folderListener) {
		listeners.add(FolderListener.class, folderListener);
	}
	
	public void removeFolderListener(FolderListener folderListener) {
		listeners.remove(FolderListener.class, folderListener);
	}
	
	public void setModel(List<Note> notes, List<Folder> folders, Folder root) {
		var rootNode = new DefaultMutableTreeNode(root);
		var folderNodes = new ArrayList<>(List.of(rootNode));
		
		folders.forEach(folder -> {
			var parentNode = folderNodes.stream().filter(treeNode -> {
				assert folder.getParent() != null;
				return treeNode.getUserObject().hashCode() == folder.getParent().hashCode();
			}).findFirst().orElse(rootNode);
			
			var folderNode = new DefaultMutableTreeNode(folder);
			
			parentNode.add(folderNode);
			folderNodes.add(folderNode);
		});
		
		notes.forEach(note -> {
			var parentNode = folderNodes.stream().filter(treeNode -> treeNode.getUserObject().hashCode() == note.getParent().hashCode()).findFirst().orElse(rootNode);
			
			parentNode.add(new DefaultMutableTreeNode(note));
		});
		
		tree.setModel(new JTree(rootNode).getModel());
	}
	
	public JTree getTree() {
		return tree;
	}
	
	@Override
	public String getID() {
		return "folder";
	}
	
	@Override
	public String getTitle() {
		return "Проект";
	}
	
	@Override
	public Component getContent() {
		return this;
	}
	
	@Override
	public int getDockingModes() {
		return DockingMode.ALL;
	}
	
	@Override
	public ImageIcon getIconOriginal() {
		return Icons.FOLDER.getIcon();
	}
}
