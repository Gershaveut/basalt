package dev.code_offline.basalt.model.note;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private long id = -1;
    private final String name;
    private final int person;

    private String text;
    private final List<Note> noteLinks;

    public Note(String name, int author, String text, List<Note> links) {
        this.name = name;
        this.person = author;
        this.text = text;
        this.noteLinks = links;
    }

    public Note(String name, int author, String text) {
        this(name, author, text, new ArrayList<>());
    }

    public Note(String name, int author) {
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

    public int getPerson() {
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

    public void setText(String text) {
        this.text = text;
    }
}
