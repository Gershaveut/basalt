package dev.code_offline.basalt.model.note;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private long id = -1;
    private String name;
    private final long person;

    private String text;
    private final List<Note> noteLinks;

    public Note(String name, long author, String text, List<Note> links) {
        this.name = name;
        this.person = author;
        this.text = text;
        this.noteLinks = links;
    }

    public Note(String name, long author, String text) {
        this(name, author, text, new ArrayList<>());
    }

    public Note(String name, long author) {
        this(name, author, "");
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

    public List<Note> getNoteLinks() {
        return noteLinks;
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
}
