package com.gershaveut.jwg.util;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Vector2DInt extends Vector2D {
    public Vector2DInt(int x, int y) {
        super(x, y);
    }

    public int getXInt() {
        return (int) getX();
    }

    public int getYInt() {
        return (int) getY();
    }
}
