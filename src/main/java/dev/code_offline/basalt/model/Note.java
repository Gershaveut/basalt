package dev.code_offline.basalt.model;

import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.model.user.User;
import org.dyn4j.dynamics.Body;

import java.util.List;

public class Note implements Node {
    private String name;
    private User author;

    private String text;

    private final Body body = new Body();

    public Note(String name, User author, String text) {
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
        return author.getName();
    }

    @Override
    public List<Node> getLinks() {
        return List.of();
    }

    @Override
    public Body getBody() {
        return body;
    }

    public String getText() {
        return text;
    }

    public User getAuthorUser() {
        return author;
    }
}
