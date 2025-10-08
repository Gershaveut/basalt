package dev.code_offline.basalt.model;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class Folder {
    private String name;
    private @Nullable Folder parent;

    public Folder(String name, @Nullable Folder parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getPath() {
        StringBuilder path = new StringBuilder();
        @Nullable Folder currnetFolder = this;

        while (currnetFolder != null) {
            path.insert(0, currnetFolder.getName() + "/");
            currnetFolder = currnetFolder.getParent();
            
            if (currnetFolder != null && hashCode() == currnetFolder.hashCode()) {
                path = new StringBuilder();
                break;
            }}

        return path.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable Folder getParent() {
        return parent;
    }

    public void setParent(@Nullable Folder parent) {
        this.parent = parent;
    }
}
