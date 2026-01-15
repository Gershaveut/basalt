package dev.code_offline.basalt.model.database;

import java.util.EventListener;
import java.util.concurrent.SynchronousQueue;

public interface DatabaseListener extends EventListener {
	void sync();
	void onLostConnection();
}
