package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.core.client.Client;
import dev.code_offline.basalt.model.graph.Node;
import org.dyn4j.dynamics.Body;

public class NoteNode extends NoteInfo implements Node {
    private final Body body = new Body();

    public NoteNode(Note note, Client client) {
	    super(note, client);
    }
    
    @Override
    public Body getBody() {
		return body;
    }
}
