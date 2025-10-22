package dev.code_offline.basalt.model.graph;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dyn4j.dynamics.Body;

import java.util.List;

public interface Node {
    String getName();
    @Nullable String getAuthor();
    List<Long> getLinks();
    
    long getId();

    Body getBody();
}
