package dev.code_offline.basalt.view.tool;

import javax.swing.*;
import java.awt.*;

public interface BasaltDockable {
    int ICON_SIZE = 15;

    String getID();
    String getTitle();
    Component getContent();
    int getDockingModes();
    ImageIcon getIconOriginal();
    
    default Icon getIcon() {
        return new ImageIcon(getIconOriginal().getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, 0));
    }
}
