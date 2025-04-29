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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class FolderPanel extends JPanel implements BasaltDockable {
    private final EventListenerList listeners = new EventListenerList();

    private final JTree tree = new JTree(new Object[0]);
    private final JPopupMenu popupMenu = new JPopupMenu();

    public FolderPanel() {
        super(new BorderLayout());

        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

        var newFile = new JMenuItem("Новый файл");
        var newFolder = new JMenuItem("Новая папка");

        var rename = new JMenuItem("Переименовать");
        var delete = new JMenuItem("Удалить");

        newFile.addActionListener(e -> {
            @Nullable TreePath treeNode = tree.getSelectionPath();

            if (treeNode != null) {
                var selected = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();
                Folder folder;

                if (selected instanceof Folder f) {
                    folder = f;
                } else {
                    folder = (Folder) ((DefaultMutableTreeNode) treeNode.getParentPath().getLastPathComponent()).getUserObject();
                }

                for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
                    listener.newFile(folder);
                }
            }
        });
        newFolder.addActionListener(e -> {
            @Nullable TreePath treeNode = tree.getSelectionPath();

            if (treeNode != null) {
                var selected = ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();
                Folder folder;

                if (selected instanceof Folder f) {
                      folder = f;
                } else {
                    folder = (Folder) ((DefaultMutableTreeNode) treeNode.getParentPath().getLastPathComponent()).getUserObject();
                }

                for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
                    listener.newFolder(folder);
                }
            }
        });
        rename.addActionListener(e -> {
            @Nullable TreePath treeNode = tree.getSelectionPath();

            if (treeNode != null) {
                var selectedNote = (Note) ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();

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
            }
        });
        delete.addActionListener(e -> {
            @Nullable TreePath treeNode = tree.getSelectionPath();

            if (treeNode != null) {
                var selectedNote = (Note) ((DefaultMutableTreeNode) treeNode.getLastPathComponent()).getUserObject();

                for (FolderListener listener : listeners.getListeners(FolderListener.class)) {
                    listener.delete(selectedNote);
                }
            }
        });

        popupMenu.add(newFile);
        popupMenu.add(newFolder);
        popupMenu.addSeparator();
        popupMenu.add(rename);
        popupMenu.add(delete);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(tree, e.getX(), e.getY());
                }
            }
        });

        add(tree, BorderLayout.CENTER);
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
