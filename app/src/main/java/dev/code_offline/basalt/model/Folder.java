package dev.code_offline.basalt.model;

import org.checkerframework.checker.nullness.qual.Nullable;

public class Folder {
    public static final String SEPARATOR = "@";
    
    private String path;

    public Folder(String path) {
        this.path = path;
    }
    
    public Folder(String path, Folder parent) {
        this(path);
        
        setParent(parent);
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getName() {
        return path.substring(path.lastIndexOf(SEPARATOR) + 1);
    }
    
    public void setName(String name) {
        path = path.substring(0, path.lastIndexOf(SEPARATOR) + 1) + name;
    }
    
    public @Nullable Folder getParent() {
        var parentPath = path.substring(0, path.lastIndexOf(SEPARATOR));
        
        if (parentPath.isEmpty())
            return null;
        
        return new Folder(parentPath);
    }
    
    public void setParent(Folder parent) {
        path = parent.getPath() + SEPARATOR + getName();
    }
}
