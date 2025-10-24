package dev.code_offline.basalt.model.recent;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import dev.code_offline.basalt.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasaltRecentStarts {
	private static final String FILE_NAME = "recents.json";

	private List<RecentStart> recentStarts = new ArrayList<>();

	public BasaltRecentStarts() {
		try {
			loadRecents();
		} catch (Exception ignored) {
			Main.logger.severe("Error load recents");
		}
	}
	
	private void loadRecents() throws Exception {
		var ignored = new File(FILE_NAME).createNewFile();
		
		var json = new Gson().fromJson(Files.readString(Path.of(FILE_NAME)), RecentStart[].class);
		
		if (json != null) {
			recentStarts = new ArrayList<>(Arrays.stream(json).toList());
		}
	}
	
	private void saveRecents() throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
		writer.write(new Gson().newBuilder().setFormattingStyle(FormattingStyle.PRETTY).create().toJson(recentStarts));
		
		writer.close();
	}
	
	public List<RecentStart> getRecentStarts() {
		return recentStarts;
	}
	
	public void addRecentStart(RecentStart recentStart) {
		if (recentStarts.stream().noneMatch(d -> d.getAddress().equals(recentStart.getAddress()))) {
			recentStarts.add(recentStart);
			
			try {
				saveRecents();
			} catch (Exception ignored) {
				Main.logger.severe("Error save recents");
			}
		}
	}
	
	public void removeRecentStart(RecentStart recentStart) {
		recentStarts.remove(recentStart);
		
		try {
			saveRecents();
		} catch (Exception ignored) {
			Main.logger.severe("Error save recents");
		}
	}
}
