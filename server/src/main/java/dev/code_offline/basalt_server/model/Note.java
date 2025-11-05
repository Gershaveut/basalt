package dev.code_offline.basalt_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    private @NotNull String name;
    private @NotNull long person;
    private @NotNull String text;
    private String path;
	
	public Note() {
	}
	
	public Note(String name, long person, String text, @Nullable String path) {
        this.name = name;
        this.person = person;
        this.text = text;
        this.path = path;
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
    
    public long getPerson() {
        return person;
    }
    
    public void setPerson(long person) {
        this.person = person;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public @Nullable String getPath() {
        return path;
    }
    
    public void setPath(@Nullable String path) {
        this.path = path;
    }
}
