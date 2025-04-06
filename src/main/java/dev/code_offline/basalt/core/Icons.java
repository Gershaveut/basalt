package dev.code_offline.basalt.core;

import javax.swing.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

public enum Icons {
    FOLDER("folder"),
    GRAPH("graph"),
    EDIT_NOTE("edit_note");

    private static final int ICON_SIZE = 30;
    private static final Color COLOR = Color.BLACK;

    private final String path;

    Icons(String path) {
        this.path = path;
    }

    public ImageIcon getIcon() {
        return getIcon(path);
    }

    public static ImageIcon getIcon(String path) {
        return new ImageIcon(filterIcon(new ImageIcon(iconPrefix(path))).getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, 0));
    }

    public static ImageIcon filterIcon (ImageIcon icon) {
        RGBImageFilter filter = new RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
                if ((rgb >> 24) != 0) { // уморительная проверка alpha канала на наличие
                    return COLOR.getRGB();
                }

                return rgb;
            }
        };

        ImageProducer producer = new FilteredImageSource(icon.getImage().getSource(), filter);
        return new ImageIcon(Toolkit.getDefaultToolkit().createImage(producer));
    }

    public static String iconPrefix(String path) {
        return Util.assetsPrefix("icon/" + path + ".png");
    }
}
