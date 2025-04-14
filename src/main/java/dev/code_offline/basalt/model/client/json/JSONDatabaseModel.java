package dev.code_offline.basalt.model.client.json;

import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;

import java.util.ArrayList;
import java.util.List;

public class JSONDatabaseModel {
    private List<Person> persons;
    private List<Note> notes;

    public JSONDatabaseModel(List<Person> persons, List<Note> notes) {
        this.persons = persons;
        this.notes = notes;
    }

    public JSONDatabaseModel() {
        this(new ArrayList<>(), new ArrayList<>());
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
}
