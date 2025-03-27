package com.gershaveut.jwg.graph;

import com.gershaveut.jwg.util.Vector2DInt;

public class NodeElement implements Node {
    private Vector2DInt position;

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
    public Vector2DInt getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector2DInt position) {
        this.position = position;
    }
}
