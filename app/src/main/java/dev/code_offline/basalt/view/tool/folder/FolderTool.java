package dev.code_offline.basalt.view.tool.folder;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.core.Util;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.NoteInfo;
import dev.code_offline.basalt.view.tool.AbstractTool;
import org.springframework.lang.Nullable;

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
import java.util.function.Consumer;


public class FolderTool extends AbstractTool {
	private final EventListenerList listeners = new EventListenerList();
	
	private final JTree tree = new JTree(new Object[0]);
	private final JPopupMenu popupMenu = new JPopupMenu();
	
	private @Nullable TreePath selectedTreePath;
	
	public FolderTool(JFrame parentFrame) {
		this.setLayout(new BorderLayout());
		
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		tree.setTransferHandler(new FolderTransferHandler(listeners));
		
		var newFile = new JMenuItem("Новый файл");
		var newFolder = new JMenuItem("Новая папка");
		
		var openFile = new JMenuItem("Открыть файл");
		
		var rename = new JMenuItem("Переименовать");
		var delete = new JMenuItem("Удалить");
		
		openFile.addActionListener(e -> {
			assert getSelectedNode() != null;
			var selectedNote = (NoteInfo) getSelectedNode();
			
			for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
				listener.openFile(selectedNote.getId());
			}
		});
		newFile.addActionListener(e -> {
			Object selected = getSelectedNode();
			Folder folder = null;
			
			if (selected != null) {
				if (selected instanceof Folder f) {
					folder = f;
				} else {
					assert getSelectedParentNode() != null;
					folder = (Folder) getSelectedParentNode();
				}
			}
			
			for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
				listener.newFile(folder);
			}
		});
		newFolder.addActionListener(e -> {
			Object selected = getSelectedNode();
			Folder folder = null;
			
			if (selected instanceof Folder f) {
				folder = f;
			}
			
			for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
				listener.newFolder(folder);
			}
		});
		rename.addActionListener(e -> {
			assert getSelectedNode() != null;
            String name;

            if (getSelectedNode() instanceof NoteInfo note) {
                name = note.getName();
            } else {
                name = ((Folder) getSelectedNode()).getName();
            }
			
			var input = JOptionPane.showInputDialog(parentFrame, "Переименовать", name, JOptionPane.PLAIN_MESSAGE);

			if (input != null && !input.isEmpty()) {
				for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
					if (getSelectedNode() instanceof NoteInfo note) {
						listener.rename(note.getId(), input);
					} else {
						var folder = (Folder) getSelectedNode();
						
						listener.rename(folder.getPath(), input);
					}
				}
			}
		});
		delete.addActionListener(e -> deleteSelection());
		
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
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int selRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					
					if (selPath != null) {
						tree.setSelectionPath(selPath);
					}
					
					if (selRow > -1) {
						tree.setSelectionRow(selRow);
					}
					
					showPopupMenu(setPopupMenuContext, e.getX(), e.getY());
				}
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (Util.isContextKey(e)) {
					showPopupMenu(setPopupMenuContext, 0, 0);
				} else if (Util.isDeleteKey(e) && getSelectedNode() != null) {
					deleteSelection();
				}
			}
		});
		tree.addTreeSelectionListener(e -> {
			selectedTreePath = e.getNewLeadSelectionPath();
		});
		
		add(new JScrollPane(tree), BorderLayout.CENTER);
	}

	private @Nullable Object getSelectedNode() {
		if (selectedTreePath == null)
			return null;
		
		return ((DefaultMutableTreeNode) selectedTreePath.getLastPathComponent()).getUserObject();
	}
	
	private @Nullable Object getSelectedParentNode() {
		if (selectedTreePath == null)
			return null;
		
		return ((DefaultMutableTreeNode) selectedTreePath.getParentPath().getLastPathComponent()).getUserObject();
	}
	
	private void deleteSelection() {
		assert getSelectedNode() != null;
		
		for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
			if (getSelectedNode() instanceof NoteInfo note) {
				listener.delete(note.getId());
			} else {
				listener.delete(((Folder) getSelectedNode()).getPath());
			}
		}
	}
	
	private void showPopupMenu(Consumer<PopupMenuContext> setPopupMenuContext, int x, int y) {
        var context = PopupMenuContext.Empty;
		
		if (getSelectedNode() instanceof NoteInfo) {
			context = PopupMenuContext.Note;
		} else if (getSelectedNode() instanceof Folder) {
			context = PopupMenuContext.Folder;
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
	
	public void setModel(List<NoteInfo> notes, List<Folder> folders) {
		var rootNode = new DefaultMutableTreeNode(new Folder());
		var folderNodes = new ArrayList<>(List.of(rootNode));
		
		folders.forEach(folder -> createFolder(folder, folderNodes, rootNode));
		
		notes.forEach(note -> {
			var parentNode = folderNodes.stream().filter(treeNode -> ((Folder) treeNode.getUserObject()).getPath().equals(note.getPath())).findFirst().orElse(rootNode);
			
			parentNode.add(new DefaultMutableTreeNode(note));
		});
		
		var state = getExpansionState();
		
		tree.setModel(new JTree(rootNode).getModel());
		
		setExpansionState(state);
	}
	
	private void createFolder(Folder folder, ArrayList<DefaultMutableTreeNode> folderNodes, DefaultMutableTreeNode root) {
		DefaultMutableTreeNode parentNode = root;
		Folder parent = folder.getParent();
	
		if (parent != null) {
			try {
				parentNode = folderNodes.stream().filter(treeNode -> ((Folder) treeNode.getUserObject()).getPath().equals(parent.getPath())).findFirst().orElseThrow();
			} catch (Exception ignored) {
			}
		}
		
		var folderNode = new DefaultMutableTreeNode(folder);
		
		parentNode.add(folderNode);
		folderNodes.add(folderNode);
		
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
	public int getDockingModes() {
		return DockingMode.ALL;
	}
	
	@Override
	public ImageIcon getIconOriginal() {
		return Icons.FOLDER.getIcon();
	}
}
