package dev.code_offline.basalt_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import org.springframework.lang.Nullable;

@Entity
public class Folder {
	public static final String SEPARATOR = "@";
	
	@Id
	private String path;
	
	public Folder() {
	}
	
	public Folder(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getName() {
		return path.substring(path.lastIndexOf(SEPARATOR) + 1);
	}
	
	public void setName(String name) {
		path = path.substring(0, path.lastIndexOf(SEPARATOR) + 1) + name;
	}
	
	public @Nullable Folder getParent() {
		var parentPath = path.substring(0, path.lastIndexOf(SEPARATOR));
		
		if (parentPath.isEmpty())
			return null;
		
		return new Folder(parentPath);
	}
	
	public void setParent(Folder parent) {
		path = parent.getPath() + SEPARATOR + getName();
	}
}
