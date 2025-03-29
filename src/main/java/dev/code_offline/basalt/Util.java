package dev.code_offline.basalt;

import org.dyn4j.geometry.Vector2;

import java.awt.*;

public class Util {
    public static Vector2 pointToVector(Point point) {
        return new Vector2(point.x, point.y);
    }
}
