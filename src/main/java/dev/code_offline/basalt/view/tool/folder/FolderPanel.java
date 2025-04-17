package dev.code_offline.basalt.view.tool.folder;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;


public class FolderPanel extends JPanel implements BasaltDockable {
    private final EventListenerList listeners = new EventListenerList();

    private final JTree tree = new JTree(new Object[0]);
    private final JPopupMenu popupMenu = new JPopupMenu();

    public FolderPanel() {
        super(new BorderLayout());

        var rename = new JMenuItem("Переименовать");
        var delete = new JMenuItem("Удалить");

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

    public void setNotes(List<Note> notes) {
        tree.setModel(new JTree(notes.toArray()).getModel());
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
