package org.gershaveut.basalt.view.tool.file;

import com.javadocking.dockable.DockingMode;
import org.gershaveut.basalt.ApplicationUtil;
import org.gershaveut.basalt.model.file.SFile;
import org.gershaveut.basalt.view.Icons;
import org.gershaveut.basalt.view.tool.AbstractTool;
import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
import org.jspecify.annotations.Nullable;

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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class FilesTool extends AbstractTool {
    private final EventListenerList listeners = new EventListenerList();

    private final JTree tree = new JTree(new Object[0]);
    private final JPopupMenu popupMenu = new JPopupMenu();
    private final JMenuItem newFile;
    private final JMenuItem newDirectory;
    private final JMenuItem openFile;
    private final JMenuItem author;
    private final JMenuItem rename;
    private final JMenuItem delete;
    private final JSeparator separator1;
    private final JSeparator separator2;

    private @Nullable TreePath selectedTreePath;
    private @Nullable Person clientPerson;

    public FilesTool(JFrame parentFrame) {
        this.setLayout(new BorderLayout());

        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.setTransferHandler(new DirectoryTransferHandler(listeners));

        newFile = new JMenuItem("Новый файл");
        newDirectory = new JMenuItem("Новая папка");

        openFile = new JMenuItem("Открыть файл");

        author = new JMenuItem("Назначить автора");
        rename = new JMenuItem("Переименовать");
        delete = new JMenuItem("Удалить");

        openFile.addActionListener(e -> {
            if (getSelectedNode() != null) {
                var selectedFile = (SFile) getSelectedNode();

                if (selectedFile.isDirectory())
                    return;
                
                for (FilesListener listener : listeners.getListeners(FilesListener.class)) {
                    listener.openFile(selectedFile.getId());
                }
            }
        });
        newFile.addActionListener(e -> {
            Object selected = getSelectedNode();
            SFile file = null;

            if (selected != null) {
                assert getSelectedParentNode() != null;
                file = (SFile) getSelectedParentNode();
            }

            for (FilesListener listener : listeners.getListeners(FilesListener.class)) {
                listener.newFile(file, false);
            }
        });
        newDirectory.addActionListener(e -> {
            Object selected = getSelectedNode();
            SFile file = (SFile) selected;

            for (FilesListener listener : listeners.getListeners(FilesListener.class)) {
                listener.newFile(file, true);
            }
        });
        author.addActionListener(e -> {
            if (getSelectedNode() != null) {
                var file = (SFile) getSelectedNode();

                var input = JOptionPane.showInputDialog(parentFrame, "Назначить автора", file.getName(), JOptionPane.PLAIN_MESSAGE);

                if (input != null && !input.isEmpty()) {
                    for (FilesListener listener : listeners.getListeners(FilesListener.class)) {
                        listener.author(file.getId(), input);
                    }
                }
            }
        });
        rename.addActionListener(e -> {
            if (getSelectedNode() != null) {
                SFile file = (SFile) getSelectedNode();

                var input = JOptionPane.showInputDialog(parentFrame, "Переименовать", file.getName(), JOptionPane.PLAIN_MESSAGE);

                if (input != null && !input.isEmpty()) {
                    for (FilesListener listener : listeners.getListeners(FilesListener.class)) {
                        listener.renameFile(file.getId(), input);
                    }
                }
            }
        });
        delete.addActionListener(e -> {
            if (getSelectedNode() != null) {
                for (FilesListener listener : listeners.getListeners(FilesListener.class)) {
                    listener.deleteFile(((SFile) getSelectedNode()).getId());
                }
            }
        });

        separator1 = new JSeparator();
        separator2 = new JSeparator();

        popupMenu.add(newFile);
        popupMenu.add(newDirectory);
        popupMenu.add(separator1);
        popupMenu.add(openFile);
        popupMenu.add(separator2);
        popupMenu.add(author);
        popupMenu.add(rename);
        popupMenu.add(delete);

        registerAccelerator(newFile, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        registerAccelerator(newDirectory, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));

        registerAccelerator(openFile, KeyStroke.getKeyStroke("ENTER"));

        registerAccelerator(author, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK));
        registerAccelerator(rename, KeyStroke.getKeyStroke("F2"));
        registerAccelerator(delete, KeyStroke.getKeyStroke("DELETE"));

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

                    showPopupMenu(e.getX(), e.getY());
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
                    showPopupMenu(0, 0);
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

    private void showPopupMenu(int x, int y) {
        updateMenuContext();

        if (ApplicationUtil.anyComponentsVisible(popupMenu))
            popupMenu.show(tree, x, y);
    }

    private void updateMenuContext() {
        var context = PopupMenuContext.Empty;

        if (getSelectedNode() instanceof SFile file) {
            if (file.isDirectory())
                context = PopupMenuContext.Directory;
            else
                context = PopupMenuContext.Note;
        }

        newFile.setVisible(false);
        newDirectory.setVisible(false);
        separator1.setVisible(false);
        openFile.setVisible(false);
        separator2.setVisible(false);
        author.setVisible(false);
        rename.setVisible(false);
        delete.setVisible(false);

        if (ApplicationUtil.hasRole(clientPerson, Role.MEMBER)) {
            newFile.setVisible(true);
            newDirectory.setVisible(true);
        }

        switch (context) {
            case PopupMenuContext.Note -> {
                openFile.setVisible(true);

                if (ApplicationUtil.accessFile(clientPerson, (SFile) Objects.requireNonNull(getSelectedNode()))) {
                    separator1.setVisible(true);

                    rename.setVisible(true);
                    delete.setVisible(true);
                    separator2.setVisible(true);
                }

                if (ApplicationUtil.hasRole(clientPerson, Role.MODERATOR))
                    author.setVisible(true);
            }
            case PopupMenuContext.Directory -> {
                if (ApplicationUtil.hasRole(clientPerson, Role.MEMBER)) {
                    rename.setVisible(true);
                    delete.setVisible(true);
                    separator2.setVisible(true);
                }
            }
            default -> {
            }
        }
    }

    public void addFilesListener(FilesListener filesListener) {
        listeners.add(FilesListener.class, filesListener);
    }

    public void removeFilesListener(FilesListener filesListener) {
        listeners.remove(FilesListener.class, filesListener);
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

    public void setModel(List<SFile> files, Person clientPerson) {
        this.clientPerson = clientPerson;

        var rootNode = new DefaultMutableTreeNode(new SFile("", "", clientPerson, true));
        var filesNodes = new ArrayList<>(List.of(rootNode));

        files.stream().sorted(Comparator.comparing(SFile::isDirectory).reversed()).forEach(file -> {
            var parent = file.getParent();

            var parentNode = filesNodes.stream().filter(treeNode -> ((SFile) treeNode.getUserObject()).getAbsolutePath().equals(parent)).findFirst().orElse(rootNode);

            var directoryNode = new DefaultMutableTreeNode(file);

            parentNode.add(directoryNode);
            filesNodes.add(directoryNode);
        });

        var state = getExpansionState();

        SwingUtilities.invokeLater(() -> {
            tree.setModel(new JTree(rootNode).getModel());
            setExpansionState(state);
        });
    }

    private void registerAccelerator(JMenuItem menuItem, KeyStroke keyStroke) {
        ApplicationUtil.registerAccelerator(menuItem, tree, keyStroke, this::updateMenuContext);
    }

    public JTree getTree() {
        return tree;
    }

    @Override
    public String getID() {
        return "files";
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
