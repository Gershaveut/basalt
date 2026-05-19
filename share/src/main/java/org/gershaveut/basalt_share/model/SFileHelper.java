package org.gershaveut.basalt_share.model;

import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.Nullable;

public class SFileHelper {
    public static final String SEPARATOR = "/";
    public static final String SEND_SEPARATOR = "@";

    public static @Nullable String getParent(String path) {
        var parent = FilenameUtils.getFullPath(path);

        if (parent.isEmpty())
            return null;

        return parent;
    }

    public static String getBaseName(String name) {
        return FilenameUtils.getBaseName(name);
    }

    public static String getExtension(String name) {
        return FilenameUtils.getExtension(name);
    }

    public static String getAbsolutePath(String path, String name) {
        return path + SEPARATOR + name;
    }

    public static String getPath(String path) {
        return path.replace(SEPARATOR, SEND_SEPARATOR);
    }

    public static String setPath(String path) {
        return path.replace(SEND_SEPARATOR, SEPARATOR);
    }
}
