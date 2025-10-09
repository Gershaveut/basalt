package dev.code_offline.basalt.view.input;

import java.util.EventListener;

public interface InputListener extends EventListener {
    void confirm(Object value);
    void cancel();
}
