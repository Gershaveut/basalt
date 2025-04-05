package dev.code_offline.basalt.model;

import dev.code_offline.basalt.core.Util;
import dev.code_offline.basalt.model.graph.Node;
import dev.code_offline.basalt.model.user.User;
import org.dyn4j.dynamics.Body;

import java.util.ArrayList;
import java.util.List;

public class Note implements Node {
    private final String name;
    private final User author;

    private final String text;
    private final List<Note> links;

    private final Body body = new Body();

    public Note(String name, User author, String text, List<Note> links) {
        this.name = name;
        this.author = author;
        this.text = text;
        this.links = links;
    }

    public Note(String name, User author, String text) {
        this(name, author, text, new ArrayList<>());
    }
    
    @Override
    public String toString() {
        return name;
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
        return Util.castList(links, Node.class);
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
