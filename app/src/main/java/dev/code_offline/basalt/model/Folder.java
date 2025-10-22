package dev.code_offline.basalt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Folder {
    public static final String SEPARATOR = "@";
    
    private String path;

    public Folder(String path) {
        if (!path.contains(SEPARATOR))
            path = SEPARATOR + path;
        
        this.path = path;
    }
    
    public Folder(String path, @Nullable Folder parent) {
        this(path);
        
        if (parent != null)
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
   
    @JsonIgnore
    public String getName() {
        return path.substring(path.lastIndexOf(SEPARATOR) + 1);
    }
    
    @JsonIgnore
    public void setName(String name) {
        path = path.substring(0, path.lastIndexOf(SEPARATOR) + 1) + name;
    }
    
    @JsonIgnore
    public @Nullable Folder getParent() {
        var parentPath = path.substring(0, path.lastIndexOf(SEPARATOR));
        
        if (parentPath.equals(SEPARATOR))
            return null;
        
        return new Folder(parentPath);
    }
    
    @JsonIgnore
    public void setParent(Folder parent) {
        path = parent.getPath() + SEPARATOR + getName();
    }
}
