package dev.code_offline.basalt.view.tool;

import javax.swing.*;
import java.awt.*;

public class FolderPanel extends JPanel {
    public FolderPanel() {
        super(new BorderLayout());

        var tree = new JTree();

        add(tree, BorderLayout.CENTER);
    }
}
