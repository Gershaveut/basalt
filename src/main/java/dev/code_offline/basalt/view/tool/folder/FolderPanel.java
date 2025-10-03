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
import javax.swing.tree.*;
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
			var selectedNote = (Note) selectedNode;
			
			var input = new InputTextFrame("Переименовать", "Переименовать - " + selectedNote.getName(), selectedNote.getName());
			input.addInputListener(new InputListener() {
				@Override
				public void confirm(Object value) {
					for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
						listener.rename(selectedNote.getId(), value.toString());
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
			var selectedNote = (Note) selectedNode;
			
			for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
				listener.delete(selectedNote);
			}
		});
		
		var separator1 = new JSeparator();
		var separator2 = new JSeparator();
		
		popupMenu.add(newFile);
		//popupMenu.add(newFolder);
		popupMenu.add(separator1);
		popupMenu.add(openFile);
		popupMenu.add(separator2);
		popupMenu.add(rename);
		popupMenu.add(delete);
		
		Consumer<Boolean> setFileOptionsVisibly = (visibly) -> {
			openFile.setVisible(visibly);
			rename.setVisible(visibly);
			delete.setVisible(visibly);
			separator1.setVisible(visibly);
			separator2.setVisible(visibly);
		};
	
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					showPopupMenu(setFileOptionsVisibly, e.getX(), e.getY());
				}
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
					showPopupMenu(setFileOptionsVisibly, 0, 0); // TODO: Назначить координату y
				}
			}
		});
		
		add(tree, BorderLayout.CENTER);
	}
	
	private void showPopupMenu(Consumer<Boolean> setFileOptionsVisibly, int x, int y) {
		@Nullable TreePath treeNode = getTree().getSelectionPath();
		
		if (treeNode != null) {
			selectedNode = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();
			selectedParentNode = ((DefaultMutableTreeNode) treeNode.getParentPath().getLastPathComponent()).getUserObject();
		}
		
		setFileOptionsVisibly.accept(treeNode != null);
		
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
			}).findFirst().orElseThrow();
			
			var folderNode = new DefaultMutableTreeNode(folder);
			
			parentNode.add(folderNode);
			folderNodes.add(folderNode);
		});
		
		notes.forEach(note -> {
			var parentNode = folderNodes.stream().filter(treeNode -> treeNode.getUserObject().hashCode() == note.getParent().hashCode()).findFirst().orElseThrow();
			
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
