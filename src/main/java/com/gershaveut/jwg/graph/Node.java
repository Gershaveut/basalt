package com.gershaveut.jwg.graph;

import com.gershaveut.jwg.util.Vector2DInt;

public interface Node {
    String getName();
    String getAuthor();
    Node getParent();

    Vector2DInt getPosition();
    void setPosition(Vector2DInt position);
}
