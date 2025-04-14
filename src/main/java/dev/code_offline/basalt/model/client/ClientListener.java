package dev.code_offline.basalt.model.client;

import java.util.EventListener;

public interface ClientListener extends EventListener {
    void sync();
}
