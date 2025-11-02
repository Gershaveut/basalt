package dev.code_offline.basalt.model.note;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private long id = -1;
    private String name;
    private long person;

    private String text;
    private @Nullable String path;
    private List<Long> links;

    public Note() {}
    
    public Note(String name, long author, String text, @Nullable String path, List<Long> links) {
        this.name = name;
        this.person = author;
        this.text = text;
        this.path = path;
        this.links = links;
    }

    public Note(String name, @Nullable String path) {
        this(name, 0, "", path, new ArrayList<>());
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

    public @Nullable String getPath() {
        return path;
    }

    @JsonIgnore
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
    }

    public void setPath(@Nullable String path) {
        this.path = path;
    }

    @JsonIgnore
    public void setLinks(List<Long> links) {
        this.links = links;
    }
}
