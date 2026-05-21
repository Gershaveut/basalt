package org.gershaveut.basalt.model.file;

import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.SFileHelper;
import org.jspecify.annotations.Nullable;

public class SFile {
    protected long id;

    private String name;
    private String path;
    private @Nullable String metadata;

    private boolean isDirectory;

    private Person person; 
    
    protected SFile() {
        path = "";
        name = "Null";
        person = null;
    }

    public SFile(String name, String path, Person person) {
        this.name = name;
        this.path = path;
        this.person = person;
    }

    public SFile(String name, Person person) {
        this(name, "", person);
    }

    public static SFile mkdir(String name, String path, Person person) {
        var dir = new SFile(name, path, person);
        dir.isDirectory = true;

        return dir;
    }

    public static SFile mkdir(String name, Person person) {
        return mkdir(name, "", person);
    }
    
    public @Nullable String getParent() {
        return SFileHelper.getParent(path);
    }

    public String getBaseName() {
        return SFileHelper.getBaseName(name);
    }

    public String getExtension() {
        return SFileHelper.getExtension(name);
    }

    public String getAbsolutePath() {
        return SFileHelper.getAbsolutePath(path, name);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return SFileHelper.getPath(path);
    }

    public void setPath(String path) {
        this.path = SFileHelper.setPath(path);
    }

    public @Nullable String getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nullable String metadata) {
        this.metadata = metadata;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public String toString() {
        return getName();
    }
}
