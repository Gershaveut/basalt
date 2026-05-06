package org.gershaveut.basalt.model.note;

import org.gershaveut.basalt.model.database.Database;
import org.gershaveut.basalt.model.graph.Node;
import org.gershaveut.basalt_server.model.Note;
import org.dyn4j.dynamics.Body;

public class NoteNode extends NoteInfo implements Node {
    private final Body body = new Body();

    public NoteNode(Note note, Database database) {
	    super(note, database);
    }
    
    @Override
    public Body getBody() {
		return body;
    }
}
