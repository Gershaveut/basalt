package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.controller.client.Client;
import dev.code_offline.basalt.model.Folder;

import java.util.List;

public class NoteInfo {
	private final long id;
    private final String name;
    private final long person;
    private final String path;
    private final List<Long> links;
    
    private String author = "Loading...";

    public NoteInfo(Note note, Client client) {
		
		this.id = note.getId();
        this.name = note.getName();
        this.person = note.getPerson();
        this.path = note.getPath();
        this.links = note.getLinks();
        
        client.getPerson(person).subscribe(p -> author = p.getName());
	}
    
    @Override
    public String toString() {
        return name;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public List<Long> getLinks() {
        return links;
    }

    public long getId() {
        return id;
    }
}
