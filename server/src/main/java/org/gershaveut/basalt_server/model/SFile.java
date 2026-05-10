package org.gershaveut.basalt_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gershaveut.basalt_share.model.Person;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.List;

@Entity
public class SFile {
    public static final String SEPARATOR = "/";
    public static final String SEND_SEPARATOR = "@";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    private @NotNull String name;
    private @NotNull String path;
    private @Nullable String metadata;
    private boolean isDirectory;
    
    @ManyToOne
    private @NotNull Person person;

    protected SFile() {
        path = "";
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
   
    @JsonIgnore
    public @Nullable String getParent() {
        var parent = FilenameUtils.getFullPath(path);
        
        if (parent.isEmpty())
            return null;
        
        return parent;
    }
   
    @JsonIgnore
    public String getBaseName() {
        return FilenameUtils.getBaseName(name);
    }
    
    @JsonIgnore
    public String getExtension() {
        return FilenameUtils.getExtension(name);
    }
    
    @JsonIgnore
    public String getAbsolutePath() {
        return path + SEPARATOR + name;
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
        return path.replace(SEPARATOR, SEND_SEPARATOR);
    }

    public void setPath(String path) {
        this.path = path.replace(SEND_SEPARATOR, SEPARATOR);
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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
    
    public File toFile() {
        return new File("./" + getAbsolutePath());
    }
}
