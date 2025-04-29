package dev.code_offline.basalt.model.client.json;

import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;

import java.util.ArrayList;
import java.util.List;

public class JSONDatabaseModel {
    private List<Person> persons;
    private List<Note> notes;
    private List<Folder> folders;
    private Folder root;

    public JSONDatabaseModel(List<Person> persons, List<Note> notes, List<Folder> folders, Folder root) {
        this.persons = persons;
        this.notes = notes;
        this.folders = folders;
        this.root = root;
    }

    public JSONDatabaseModel() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Folder("/", null));
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public Folder getRoot() {
        return root;
    }

    public void setRoot(Folder root) {
        this.root = root;
    }
}
