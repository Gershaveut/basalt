package com.gershaveut.jwg.util;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class Point2DInt extends Point2D implements Serializable {
    public int x;
    public int y;

    public Point2DInt() {
    }

    public Point2DInt(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getXInt() {
        return x;
    }

    public int getYInt() {
        return y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setLocation(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public void setLocation(float x, float y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public String toString() {
        return "Point2D.Int["+x+", "+y+"]";
    }
}
