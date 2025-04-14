package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.model.note.Note;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class FolderPanel extends JPanel implements BasaltDockable {
    private final JTree tree = new JTree(new Object[0]);

    public FolderPanel() {
        super(new BorderLayout());

        add(tree, BorderLayout.CENTER);
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
