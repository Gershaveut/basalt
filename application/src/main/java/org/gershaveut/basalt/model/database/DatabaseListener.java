package org.gershaveut.basalt.model.database;

import java.util.EventListener;

public interface DatabaseListener extends EventListener {
	void sync();
	void onLostConnection();
}
