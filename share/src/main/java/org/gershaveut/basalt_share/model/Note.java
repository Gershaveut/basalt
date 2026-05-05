package org.gershaveut.basalt_share.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Note extends File {
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> links = new ArrayList<>();
	
	private Note() {
        super("Null", 0, new byte[0], null);
    }
	
	public Note(String name, long person, String text, @Nullable String path) {
        super(name + ".md", person, text.getBytes(), path);
    }
    
    public Note(String name, @Nullable String path) {
        this(name, 0, "", path);
    }
 
    public String getText() {
        return new String(getRawContent());
    }
    
    public List<Long> getLinks() {
        return links;
    }
    
    public void setLinks(List<Long> links) {
        this.links = links;
    }
}
