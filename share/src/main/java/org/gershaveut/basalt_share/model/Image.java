package org.gershaveut.basalt_share.model;

import jakarta.persistence.Entity;
import org.jspecify.annotations.Nullable;

@Entity
public class Image extends File {
   
    private Image() {
        super("Null", 0, new byte[0], null);
    }
    
    public Image(String name, long person, @Nullable String path, byte[] content) {
        super(name, person, content, path);
    }
}
