package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.model.database.Database;
import org.springframework.lang.Nullable;

import java.util.List;

public class NoteInfo {
	private final long id;
    private final String name;
    private final long person;
    private final @Nullable String path;
    private final List<Long> links;
    
    private String author = "Loading...";

    public NoteInfo(Note note, Database database) {
		
		this.id = note.getId();
        this.name = note.getName();
        this.person = note.getPerson();
        this.path = note.getPath();
        this.links = note.getLinks();
        
        // database.getPerson(person).subscribe(p -> author = p.getName()); TODO: временно отключено
	}
    
    @Override
    public String toString() {
        return name;
    }
    
    public @Nullable String getPath() {
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
