package dev.code_offline.basalt.view.tool;

import org.springframework.lang.Nullable;

import javax.swing.*;
import java.awt.*;

public interface BasaltDockable {
    int ICON_SIZE = 15;

    String getID();
    String getTitle();
    Component getContent();
    int getDockingModes();
    @Nullable ImageIcon getIconOriginal();

    @Nullable default Icon getIcon() {
        if (getIconOriginal() != null)
            return new ImageIcon(getIconOriginal().getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, 0));
        else
            return null;
    }
}
