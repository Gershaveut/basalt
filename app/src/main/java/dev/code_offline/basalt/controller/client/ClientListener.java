package dev.code_offline.basalt.controller.client;

import java.util.EventListener;

public interface ClientListener extends EventListener {
    void sync();
    void onLostConnection();
}
