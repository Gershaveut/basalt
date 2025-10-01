package dev.code_offline.basalt.model.note;

import dev.code_offline.basalt.model.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Note {
    private long id = -1;
    private String name;
    private final long person;

    private String text;
    private Folder parent;
    private List<Long> links;

    public Note(String name, long author, String text, Folder parent, List<Long> links) {
        this.name = name;
        this.person = author;
        this.text = text;
        this.parent = parent;
        this.links = links;
        
        loadLinks();
    }

    public Note(String name, long author, String text, Folder parent) {
        this(name, author, text, parent, new ArrayList<>());
    }

    public Note(String name, long author, Folder folder) {
        this(name, author, "", folder);
    }
   
    private void loadLinks() {
        var links = new ArrayList<Long>();
        
        Pattern pattern = Pattern.compile("\\[([^]]*)");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            var number = Long.parseLong(matcher.group(1).trim());
            
            if (number != id)
                links.add(number);
        }
        
        this.links = links;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPerson() {
        return person;
    }

    public String getText() {
        return text;
    }

    public Folder getParent() {
        return parent;
    }

    public List<Long> getLinks() {
        return links;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
       
        loadLinks();
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }
}
