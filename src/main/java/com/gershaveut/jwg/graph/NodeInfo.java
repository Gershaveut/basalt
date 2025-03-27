package com.gershaveut.jwg.graph;

import com.gershaveut.jwg.util.Point2DInt;

public interface NodeInfo {
    String getName();
    String getAuthor();
    NodeInfo getParent();
    Point2DInt getPosition();
}
