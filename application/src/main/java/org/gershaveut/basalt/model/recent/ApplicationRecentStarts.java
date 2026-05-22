package org.gershaveut.basalt.model.recent;

import org.gershaveut.basalt_share.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationRecentStarts {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRecentStarts.class);
	
	private static final String FILE_NAME = Util.savePrefix("recents.json");

	private List<RecentStart> recentStarts = new ArrayList<>();

	public ApplicationRecentStarts() {
		try {
			loadRecents();
		} catch (Exception exception) {
			LOGGER.error("Error load recents", exception);
		}
	}
	
	private void loadRecents() throws Exception {
		var created = new File(FILE_NAME).createNewFile();
		
		if (!created) {
			var json = Util.getMapper().readValue(Files.readString(Path.of(FILE_NAME)), RecentStart[].class);
			
			if (json != null) {
				recentStarts = new ArrayList<>(Arrays.stream(json).toList());
			}
		}
	}
	
	private void saveRecents() throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
		writer.write(Util.getMapper().writeValueAsString(recentStarts));
		
		writer.close();
	}
	
	public List<RecentStart> getRecentStarts() {
		return recentStarts;
	}
	
	public void addRecentStart(RecentStart recentStart) {
		if (recentStarts.stream().noneMatch(d -> d.getAddress().equals(recentStart.getAddress()))) {
			recentStarts.add(recentStart);
			
			trySaveRecents();
		}
	}
	
	public void removeRecentStart(RecentStart recentStart) {
		recentStarts.remove(recentStart);
		
		trySaveRecents();
	}
	
	private void trySaveRecents() {
		try {
			saveRecents();
		} catch (Exception ignored) {
			LOGGER.error("Error save recents");
		}
	}
}
