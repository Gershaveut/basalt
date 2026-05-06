package org.gershaveut.basalt_server.model;

import jakarta.persistence.Entity;
import org.gershaveut.basalt_share.Util;
import org.gershaveut.basalt_share.model.Person;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;

@Entity
public class Note extends File {
	private Note() {
        this("Null", "");
    }
	
	public Note(String name, String path, Person person) {
        super(name + ".md", path, person);
    }
    
    public Note(String name, String path) {
        this(name, path, null);
    }
 
    public String getText() {
        return new String(getContent());
    }

    public void setText(String text) {
        setContent(text.getBytes());
    }
    
    public List<Long> getLinks() {
        return Util.getMapper().readValue(getMetadata(), new TypeReference<>() {});
    }
    
    public void setLinks(List<Long> links) {
        setMetadata(Util.getMapper().writeValueAsString(links));
    }
}
