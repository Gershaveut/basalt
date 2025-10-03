package dev.code_offline.basalt.core.client;

import java.util.EventListener;

public interface ClientListener extends EventListener {
    void sync();
}
