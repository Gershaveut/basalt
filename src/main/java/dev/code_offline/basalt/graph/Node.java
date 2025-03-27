package dev.code_offline.basalt.graph;

import org.dyn4j.dynamics.Body;

public interface Node {
    String getName();
    String getAuthor();
    Node getParent();

    Body getBody();
}
