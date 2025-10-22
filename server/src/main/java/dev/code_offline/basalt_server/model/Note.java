package dev.code_offline.basalt_server.model;

import jakarta.persistence.*;

@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    private String name;
    private long person;
    private String text;
    private String path;
	
	public Note() {
	}
	
	public Note(String name, long person, String text, String path) {
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
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}
