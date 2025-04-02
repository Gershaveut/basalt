package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.model.graph.Node;
import org.dyn4j.dynamics.Body;

import java.util.List;

public class Note implements Node {
    public String name;
    public String author;

    public String text;

    private final Body body = new Body();

    public Note(String name, String author, String text) {
        this.name = name;
        this.author = author;
        this.text = text;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public List<Node> getLinks() {
        return List.of();
    }

    @Override
    public Body getBody() {
        return body;
    }
}
