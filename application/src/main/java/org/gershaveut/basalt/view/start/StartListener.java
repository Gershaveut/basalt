package org.gershaveut.basalt.view.start;

import org.gershaveut.basalt.model.recent.RecentStart;

import java.util.EventListener;

public interface StartListener extends EventListener {
	void openDatabase(String path);
	void connectDatabase(String address);
	
	void deleteRecentStart(RecentStart recentStart);
}
