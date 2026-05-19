package org.gershaveut.basalt.model.graph;

import org.dyn4j.dynamics.Body;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface Node {
    String getName();
    @Nullable String getAuthor();
    List<String> getLinks();
    
    long getId();

    Body getBody();
}
