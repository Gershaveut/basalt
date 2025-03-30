package dev.code_offline.basalt.view.component.graph;

import org.dyn4j.dynamics.Body;

import java.util.List;

public interface Node {
    String getName();
    String getAuthor();
    List<Node> getLinks();

    Body getBody();
}
