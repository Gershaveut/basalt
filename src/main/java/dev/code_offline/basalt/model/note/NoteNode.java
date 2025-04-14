package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.model.graph.Node;
import org.dyn4j.dynamics.Body;

import java.util.ArrayList;
import java.util.List;

public class NoteNode extends Note implements Node {
    private final Body body = new Body();

    public NoteNode(String name, int author, String text, List<Note> links) {
        super(name, author, text, links);
    }

    public NoteNode(String name, int author, String text) {
        super(name, author, text);
    }

    public NoteNode(Note note) {
        super(note.getName(), note.getPerson(), note.getText(), note.getNoteLinks());
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getAuthor() {
        return "TODO: В разработке";
    }

    @Override
    public List<Node> getLinks() {
        return new ArrayList<>(super.getNoteLinks().stream().map(NoteNode::new).toList());
    }

    @Override
    public Body getBody() {
        return body;
    }
}
