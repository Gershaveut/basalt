package org.gershaveut.basalt_share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Base64;

@Entity
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private @NotNull long person;
    private @NotNull String name;
    private @NotNull @Lob byte[] content;
    private @Nullable String path;
   
    private File() {
        name = "";
        content = new byte[0];
    }
    
    public File(String name, long person, byte[] content, @Nullable String path) {
       this.name = name;
       this.person = person;
       this.content = content;
       this.path = path;
    }

    public long getId() {
        return id;
    }

    public long getPerson() {
        return person;
    }

    public void setPerson(long person) {
        this.person = person;
    }

    public String getName() {
        return name;
    }
   
    @JsonIgnore
    public String getBaseName() {
        return name.substring(0, name.lastIndexOf("."));
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = Base64.getDecoder().decode(content);
    }

    @JsonIgnore
    public String getContent() {
        return Base64.getEncoder().encodeToString(content);
    }
    
    @JsonIgnore
    public byte[] getRawContent() {
        return content;
    }
    
    public @Nullable String getPath() {
        return path;
    }

    public void setPath(@Nullable String path) {
        this.path = path;
    }

    public String getAbsolutePath() {
        var path = "";

        if (this.path != null) {
            path = this.path;
        }

        return path + Folder.SEPARATOR + name;
    }

    @JsonIgnore
    public String getExtension() {
        return name.substring(name.lastIndexOf(".") + 1);
    }
}
