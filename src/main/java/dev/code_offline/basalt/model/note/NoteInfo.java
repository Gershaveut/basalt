package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.core.client.Client;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.graph.Node;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.dyn4j.dynamics.Body;

import java.util.List;

public class NoteInfo {
    private final Client client;

    private final long id;
    private final String name;
    private final long person;
    private final Folder parent;
    private final List<Long> links;

    public NoteInfo(Note note, Client client) {
        this.client = client;

        this.id = note.getId();
        this.name = note.getName();
        this.person = note.getPerson();
        this.parent = note.getParent();
        this.links = note.getLinks();
	}
    
    @Override
    public String toString() {
        return name;
    }
    
    public Folder getParent() {
        return parent;
    }
    
    public String getName() {
        return name;
    }

    public String getAuthor() {
        return client.getPerson(person).getName();
    }

    public List<Long> getLinks() {
        return links;
    }

    public long getId() {
        return id;
    }
}
