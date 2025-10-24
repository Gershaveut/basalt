package dev.code_offline.basalt.view.start;

import dev.code_offline.basalt.model.recent.RecentStart;

import java.util.EventListener;

public interface StartListener extends EventListener {
	void openDatabase(String path);
	void connectDatabase(String address);
	
	void deleteRecentStart(RecentStart recentStart);
}
