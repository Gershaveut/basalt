package dev.code_offline.basalt.core;

import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class Util {
    public static Vector2 pointToVector(Point point) {
        return new Vector2(point.x, point.y);
    }

    public static String assetsPrefix(String path) {
        return "assets/" + path;
    }
    
    public static <T> void foreachNonList(Supplier<Integer> count, Function<Integer, T> getter, Consumer<T> action) {
        for (int i = count.get(); i > 0; i--) {
            try {
                action.accept(getter.apply(i));
            } catch (IndexOutOfBoundsException ignored) {
            
            }
        }
    }
}
