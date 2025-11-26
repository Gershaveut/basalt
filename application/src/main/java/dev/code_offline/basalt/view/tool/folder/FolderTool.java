package dev.code_offline.basalt.view.tool.folder;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.ApplicationUtil;
import dev.code_offline.basalt_share.model.Folder;
import dev.code_offline.basalt.model.note.NoteInfo;
import dev.code_offline.basalt_share.model.Person;
import dev.code_offline.basalt_share.model.Role;
import dev.code_offline.basalt.view.Icons;
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
import java.util.Objects;
import java.util.function.Consumer;


public class FolderTool extends AbstractTool {
	private final EventListenerList listeners = new EventListenerList();
	
	private final JTree tree = new JTree(new Object[0]);
	private final JPopupMenu popupMenu = new JPopupMenu();
	
	private @Nullable TreePath selectedTreePath;
	private @Nullable Person clientPerson;
	
	public FolderTool(JFrame parentFrame) {
		this.setLayout(new BorderLayout());
		
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		tree.setTransferHandler(new FolderTransferHandler(listeners));
		
		var newFile = new JMenuItem("Новый файл");
		var newFolder = new JMenuItem("Новая папка");
		
		var openFile = new JMenuItem("Открыть файл");
		
		var author = new JMenuItem("Назначить автора");
		var rename = new JMenuItem("Переименовать");
		var delete = new JMenuItem("Удалить");
		
		registerAccelerator(newFile, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		registerAccelerator(newFolder, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
		
		registerAccelerator(openFile, KeyStroke.getKeyStroke("ENTER"));
		
		registerAccelerator(author, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK));
		registerAccelerator(rename, KeyStroke.getKeyStroke("F2"));
		registerAccelerator(delete, KeyStroke.getKeyStroke("DELETE"));
		
		openFile.addActionListener(e -> {
			if (getSelectedNode() != null) {
				var selectedNote = (NoteInfo) getSelectedNode();
				
				for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
					listener.openFile(selectedNote.getId());
				}
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
		author.addActionListener(e -> {
			if (getSelectedNode() != null) {
				NoteInfo note = (NoteInfo) getSelectedNode();
				
				var input = JOptionPane.showInputDialog(parentFrame, "Назначить автора", note.getName(), JOptionPane.PLAIN_MESSAGE);
				
				if (input != null && !input.isEmpty()) {
					for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
						listener.author(note.getId(), input);
					}
				}
			}
		});
		rename.addActionListener(e -> {
			if (getSelectedNode() != null) {
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
							listener.renameNote(note.getId(), input);
						} else {
							var folder = (Folder) getSelectedNode();
							
							listener.renameFolder(folder.getPath(), input);
						}
					}
				}
			}
		});
		delete.addActionListener(e -> {
			if (getSelectedNode() != null) {
				for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
					if (getSelectedNode() instanceof NoteInfo note) {
						listener.deleteNote(note.getId());
					} else {
						listener.deleteFolder(((Folder) getSelectedNode()).getPath());
					}
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
		popupMenu.add(author);
		popupMenu.add(rename);
		popupMenu.add(delete);
		
		Consumer<PopupMenuContext> setPopupMenuContext = (context) -> {
			newFile.setVisible(false);
			newFolder.setVisible(false);
			separator1.setVisible(false);
            openFile.setVisible(false);
			separator2.setVisible(false);
			author.setVisible(false);
            rename.setVisible(false);
            delete.setVisible(false);

			if (ApplicationUtil.hasRole(clientPerson, Role.MEMBER)) {
				newFile.setVisible(true);
				newFolder.setVisible(true);
			}
			
            switch (context) {
                case PopupMenuContext.Note -> {
					openFile.setVisible(true);
				
					if (ApplicationUtil.accessNote(clientPerson, (NoteInfo) Objects.requireNonNull(getSelectedNode()))) {
						separator1.setVisible(true);
						
						rename.setVisible(true);
						delete.setVisible(true);
						separator2.setVisible(true);
					}
					
					if (ApplicationUtil.hasRole(clientPerson, Role.MODERATOR))
						author.setVisible(true);
                }
                case PopupMenuContext.Folder -> {
					if (ApplicationUtil.hasRole(clientPerson, Role.MEMBER)) {
						rename.setVisible(true);
						delete.setVisible(true);
						separator2.setVisible(true);
					}
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
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					openFile.doClick();
				}
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (ApplicationUtil.isContextKey(e)) {
					showPopupMenu(setPopupMenuContext, 0, 0);
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
	
	private void showPopupMenu(Consumer<PopupMenuContext> setPopupMenuContext, int x, int y) {
		var context = PopupMenuContext.Empty;
			
		if (getSelectedNode() instanceof NoteInfo) {
			context = PopupMenuContext.Note;
		} else if (getSelectedNode() instanceof Folder) {
			context = PopupMenuContext.Folder;
		}
		
		setPopupMenuContext.accept(context);
	
		if (ApplicationUtil.anyComponentsVisible(popupMenu))
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
	
	public void setModel(List<NoteInfo> notes, List<Folder> folders, Person clientPerson) {
		this.clientPerson = clientPerson;
		
		var rootNode = new DefaultMutableTreeNode(new Folder());
		var folderNodes = new ArrayList<>(List.of(rootNode));
		
		folders.forEach(folder -> createFolder(folder, folderNodes, rootNode));
		
		notes.forEach(note -> {
			var parentNode = folderNodes.stream().filter(treeNode -> ((Folder) treeNode.getUserObject()).getPath().equals(note.getPath())).findFirst().orElse(rootNode);
			
			parentNode.add(new DefaultMutableTreeNode(note));
		});
		
		var state = getExpansionState();
	
		SwingUtilities.invokeLater(() -> {
			tree.setModel(new JTree(rootNode).getModel());
			setExpansionState(state);
		});
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
	
	private void registerAccelerator(JMenuItem menuItem, KeyStroke keyStroke) {
		ApplicationUtil.registerAccelerator(menuItem, tree, keyStroke);
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
