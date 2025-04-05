package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;

import javax.swing.*;
import java.awt.*;

public class FolderPanel extends JPanel implements BasaltDockable {
    public FolderPanel() {
        super(new BorderLayout());

        var tree = new JTree();

        add(tree, BorderLayout.CENTER);
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
