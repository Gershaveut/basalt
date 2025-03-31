package dev.code_offline.basalt.view;

import dev.code_offline.basalt.Main;

import javax.swing.*;

public enum Icon {
    FOLDER("folder"),
    GRAPH("graph");

    private static final int ICON_SIZE = 50;

    private String path;

    Icon(String path) {
        this.path = path;
    }

    public ImageIcon getIcon() {
        return getIcon(path);
    }

    public static ImageIcon getIcon(String path) {
        return new ImageIcon(new ImageIcon(iconPrefix(path)).getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, 0));
    }

    public static String iconPrefix(String path) {
        return Main.assetsPrefix("icon/" + path + ".png");
    }
}
