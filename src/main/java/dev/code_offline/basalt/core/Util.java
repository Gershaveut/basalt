package dev.code_offline.basalt.core;

import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;


public class Util {
    public static Vector2 pointToVector(Point point) {
        return new Vector2(point.x, point.y);
    }

    public static String assetsPrefix(String path) {
        return "resources/assets/" + path;
    }
    
    public static <T> void foreachNonList(int count, Function<Integer, T> getter, Consumer<T> action) {
        for (int i = 0; i < count; i++) {
            action.accept(getter.apply(i));
        }
    }
}
