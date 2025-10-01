package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.model.client.Client;
import dev.code_offline.basalt.model.graph.Node;
import org.dyn4j.dynamics.Body;

import java.util.ArrayList;
import java.util.List;

public class NoteNode extends Note implements Node {
    private final Body body = new Body();
    private final Client client;
    
    public NoteNode(Note note, Client client) {
        super(note.getName(), note.getPerson(), note.getText(), note.getParent(), note.getLinks());
        
        this.client = client;
        
        this.setId(note.getId());
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getAuthor() {
        return client.getPerson(super.getPerson()).getName();
    }

    @Override
    public Body getBody() {
        return body;
    }
}
