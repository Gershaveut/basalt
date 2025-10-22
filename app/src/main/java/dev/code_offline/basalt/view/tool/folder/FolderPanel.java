package dev.code_offline.basalt.view.tool.folder;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.NoteInfo;
import dev.code_offline.basalt.view.tool.BasaltDockable;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class FolderPanel extends JPanel implements BasaltDockable {
	private final EventListenerList listeners = new EventListenerList();
	
	private final JTree tree = new JTree(new Object[0]);
	private final JPopupMenu popupMenu = new JPopupMenu();
	
	private @Nullable Object selectedNode;
	private @Nullable Object selectedParentNode;
	
	public FolderPanel() {
		super(new BorderLayout());
		
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		tree.setTransferHandler(new FolderTransferHandler(listeners));
		tree.setCellRenderer(new FolderTreeCellRenderer());
		
		var newFile = new JMenuItem("Новый файл");
		var newFolder = new JMenuItem("Новая папка");
		
		var openFile = new JMenuItem("Открыть файл");
		
		var rename = new JMenuItem("Переименовать");
		var delete = new JMenuItem("Удалить");
		
		openFile.addActionListener(e -> {
			assert selectedNode != null;
			var selectedNote = (NoteInfo) selectedNode;
			
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
				listener.newFolder(folder);
			}
		});
		rename.addActionListener(e -> {
			assert selectedNode != null;
            String name;

            if (selectedNode instanceof NoteInfo note) {
                name = note.getName();
            } else {
                name = ((Folder) selectedNode).getName();
            }
			
			var input = JOptionPane.showInputDialog(this, "Переименовать", name, JOptionPane.PLAIN_MESSAGE);

			for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
				if (selectedNode instanceof NoteInfo note) {
					listener.rename(note.getId(), input);
				} else {
					var folder = (Folder) selectedNode;

					listener.rename(folder.getPath(), input);
				}
			}
		});
		delete.addActionListener(e -> {
			assert selectedNode != null;

            for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
                if (selectedNode instanceof NoteInfo note) {
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
		
		add(new JScrollPane(tree), BorderLayout.CENTER);
	}
	
	private void showPopupMenu(Consumer<PopupMenuContext> setPopupMenuContext, int x, int y) {
		@Nullable TreePath treeNode = getTree().getSelectionPath();
        var context = PopupMenuContext.Empty;

		if (treeNode != null) {
			selectedNode = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();
			selectedParentNode = ((DefaultMutableTreeNode) treeNode.getParentPath().getLastPathComponent()).getUserObject();

            if (selectedNode instanceof NoteInfo) {
                context = PopupMenuContext.Note;
            } else if (selectedNode instanceof Folder) {
                context = PopupMenuContext.Folder;
            }
		} else {
			selectedNode = null;
			selectedParentNode = null;
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
	
	public String getExpansionState() {
		var string = new StringBuilder();
		
		for (int i = 0; i < tree.getRowCount(); i++) {
			TreePath treePath = tree.getPathForRow(i);
			
			if (tree.isExpanded(i)) {
				string.append(treePath.toString());
				string.append(",");
			}
		}
		
		return string.toString();
	}
	
	public void setExpansionState(String state) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			TreePath treePath = tree.getPathForRow(i);
			
			if (state.contains(treePath.toString())) {
				tree.expandRow(i);
			}
		}
	}
	
	public void setModel(List<NoteInfo> notes, List<Folder> folders, Folder root) {
		var rootNode = new DefaultMutableTreeNode(root);
		var folderNodes = new ArrayList<>(List.of(rootNode));
		
		folders.forEach(folder -> createFolder(folder, folderNodes, rootNode));
		
		notes.forEach(note -> {
			var parentNode = folderNodes.stream().filter(treeNode -> treeNode.getUserObject().hashCode() == note.getPath().hashCode()).findFirst().orElse(rootNode);
			
			parentNode.add(new DefaultMutableTreeNode(note));
		});
		
		var state = getExpansionState();
		
		tree.setModel(new JTree(rootNode).getModel());
		
		setExpansionState(state);
	}
	
	private @Nullable DefaultMutableTreeNode createFolder(Folder folder, ArrayList<DefaultMutableTreeNode> folderNodes, DefaultMutableTreeNode root) {
		DefaultMutableTreeNode parentNode = root;
		@Nullable Folder parent = folder.getParent();
	
		if (folderNodes.stream().anyMatch(treeNode -> ((Folder) treeNode.getUserObject()).getPath().equals(folder.getPath())))
			return null;
		
		if (parent != null) {
			try {
				parentNode = folderNodes.stream().filter(treeNode -> ((Folder) treeNode.getUserObject()).getPath().equals(parent.getPath())).findFirst().orElseThrow();
			} catch (Exception ignored) {
				parentNode = Objects.requireNonNull(createFolder(parent, folderNodes, root));
			}
		}
		
		var folderNode = new DefaultMutableTreeNode(folder);
		
		parentNode.add(folderNode);
		folderNodes.add(folderNode);
		
		return folderNode;
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
