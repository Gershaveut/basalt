package dev.code_offline.basalt.view.component.graph;

import org.dyn4j.dynamics.Body;

import java.util.List;

public class NodeElement implements Node {
    private final Body body = new Body();
    private final String name;
    private final String author;
    private final List<Node> links;

    public NodeElement(String name, String author, List<Node> links) {
        this.name = name;
        this.author = author;
        this.links = links;
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
        return links;
    }

    @Override
    public Body getBody() {
        return body;
    }
}
