package org.gershaveut.basalt.view;

import org.gershaveut.basalt.Application;
import org.gershaveut.basalt.ApplicationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.Objects;

public enum Icons {
    FOLDER("folder"),
    GRAPH("graph"),
    EDIT_NOTE("edit_note"),
    EDIT("edit"),
    PREVIEW("preview"),
    STACK("stack"),
    APPLICATION("application"),
    SAVE("save"),
    TERMINAL("terminal"),
    PERSON("person"),
    ARTICLE_PERSON("article_person");

    private static final Color COLOR = Color.GRAY;

    private final String path;

    Icons(String path) {
        this.path = path;
    }

    public ImageIcon getIcon() {
        return getIcon(path);
    }

    public ImageIcon getIcon(int size) {
        return new ImageIcon(getIcon().getImage().getScaledInstance(size, size, 0));
    }

    public ImageIcon getRawIcon() {
        return getRawIcon(path);
    }

    public static ImageIcon getRawIcon(String path) {
        return new ImageIcon(Objects.requireNonNull(Application.class.getClassLoader().getResource(iconPrefix(path))));
    }

    public static ImageIcon getIcon(String path) {
        return new ImageIcon(filterIcon(getRawIcon(path)).getImage());
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
        return ApplicationUtil.assetsPrefix("icon/" + path + ".png");
    }
}
