package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.core.client.Client;
import dev.code_offline.basalt.model.graph.Node;
import org.dyn4j.dynamics.Body;

import java.util.List;

public class NoteNode implements Node {
    private final Body body = new Body();
    private final Client client;

    private final long id;
    private final String name;
    private final long person;
    private final List<Long> links;

    public NoteNode(Note note, Client client) {
        this.client = client;

        this.id = note.getId();
        this.name = note.getName();
        this.person = note.getPerson();
        this.links = note.getLinks();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthor() {
        return client.getPerson(person).getName();
    }

    @Override
    public List<Long> getLinks() {
        return links;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Body getBody() {
        return body;
    }
}
