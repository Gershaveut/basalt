package dev.code_offline.basalt_share.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    private @NotNull String name;
    private @NotNull long person;
    private @NotNull String text;
    private @Nullable String path;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> links = new ArrayList<>();
	
	private Note() {
	}
	
	public Note(String name, long person, String text, @Nullable String path) {
        this.name = name;
        this.person = person;
        this.text = text;
        this.path = path;
    }
    
    public Note(String name, @Nullable String path) {
        this(name, 0, "", path);
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
    
    public List<Long> getLinks() {
        return links;
    }
    
    public void setLinks(List<Long> links) {
        this.links = links;
    }
}
