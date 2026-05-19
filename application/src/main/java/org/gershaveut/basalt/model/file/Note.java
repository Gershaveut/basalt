package org.gershaveut.basalt.model.file;

import org.dyn4j.dynamics.Body;
import org.gershaveut.basalt.model.graph.Node;
import org.gershaveut.basalt_share.Util;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class Note extends SFile implements Node {
    private final Body body = new Body();

    public List<String> getLinks() {
        return List.of(Util.getMapper().readValue(getMetadata(), String[].class));
    }

    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public @Nullable String getAuthor() {
        return getPerson().getUsername();
    }
    
    @Override
    public Body getBody() {
        return body;
    }
}
