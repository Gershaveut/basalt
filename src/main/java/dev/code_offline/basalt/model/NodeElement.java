package dev.code_offline.basalt.model;

import org.dyn4j.dynamics.Body;

public class NodeElement implements Node {
    private final Body body = new Body();
    private final String name;
    private final String author;
    private final Node parent;

    public NodeElement(String name, String author, NodeElement parent) {
        this.name = name;
        this.author = author;
        this.parent = parent;
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
    public Node getParent() {
        return parent;
    }

    @Override
    public Body getBody() {
        return body;
    }
}
