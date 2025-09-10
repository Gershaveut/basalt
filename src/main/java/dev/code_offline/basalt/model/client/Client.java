package dev.code_offline.basalt.model.client;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;

import javax.swing.event.EventListenerList;
import java.util.List;

public abstract class Client {
    private final EventListenerList listeners = new EventListenerList();

    protected Client() {
        Main.logger.info("Initializing client...");
    }

    public abstract boolean isOffline();
    
    public abstract List<Person> getPersons();
    public abstract List<Note> getNotes();
    public abstract List<Folder> getFolders();
    public abstract Folder getRoot();
    public abstract void addPerson(Person person);
    public abstract void addNote(Note note);
    public abstract void addFolder(Folder folder);
    public abstract void renameFolder(String path, String newName);
    public abstract void deleteFolder(String path);
    public abstract Person getClientPerson();
    public abstract void editNote(long id, String newText);
    public abstract void renameNote(long id, String newName);
    public abstract void deleteNote(Note note);

    public void addDatabaseListener(ClientListener clientListener) {
        listeners.add(ClientListener.class, clientListener);
    }

    public void removeDatabaseListener(ClientListener clientListener) {
        listeners.remove(ClientListener.class, clientListener);
    }

    protected void notifyListeners() {
        for (ClientListener listener : listeners.getListeners(ClientListener.class)) {
            listener.sync();
        }
    }
}
