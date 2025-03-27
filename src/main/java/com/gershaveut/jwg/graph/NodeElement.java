package com.gershaveut.jwg.graph;

import com.gershaveut.jwg.util.Point2DInt;

import java.awt.geom.Point2D;

public class NodeElement implements NodeInfo {
    public Point2DInt position = new Point2DInt();

    private String name;
    private String author;
    private NodeInfo parent;

    public NodeElement(NodeInfo nodeInfo) {
        name = nodeInfo.getName();
        author = nodeInfo.getAuthor();
        parent = nodeInfo.getParent();
        position = nodeInfo.getPosition();
    }

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
    public NodeInfo getParent() {
        return parent;
    }

    @Override
    public Point2DInt getPosition() {
        return position;
    }
}
