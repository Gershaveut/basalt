package dev.code_offline.basalt.controller.Database;

import java.util.EventListener;

public interface DatabaseListener extends EventListener {
	void sync();
	void onLostConnection();
}
