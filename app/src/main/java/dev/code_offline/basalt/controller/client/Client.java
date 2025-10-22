package dev.code_offline.basalt.controller.client;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.model.database.Database;
import dev.code_offline.basalt.model.database.DatabaseListener;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;
import dev.code_offline.basalt.model.person.Role;
import reactor.core.publisher.Mono;

import javax.swing.event.EventListenerList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Client implements DatabaseListener {
    private final EventListenerList listeners = new EventListenerList();
    
    private final Database database;
    
    private final Folder root = new Folder("root");
    private final Person clientPerson = new Person("Вы", 0, Role.ADMIN, null);
    
    private boolean offline = false;
    
    public Client(Database database) {
        Main.logger.info("Initializing client...");
       
		database.addDatabaseListener(this);
		
        this.database = database;
    }
    
	public void close() {
		database.close();
	}

    public boolean isOffline() {
        return offline;
    }
	
	public Mono<List<Person>> getPersons() {
		return database.getPersons();
	}
	
	public Mono<List<Note>> getNotes() {
		return database.getNotes();
	}
	
	public Mono<List<Folder>> getFolders() {
		return database.getFolders();
	}
	
	public Folder getRoot() {
		return root;
	}
	
	public Mono<Note> getNote(long id) {
		return database.getNote(id);
	}
	
	public Mono<Person> getPerson(long id) {
		return database.getPerson(id);
	}
	
	public void addPerson(Person person) {
	    database.addPerson(person);
	}
	
	public void addNote(Note note) {
	    database.addNote(note);
	}
	
	public void addFolder(Folder folder) {
	    database.addFolder(folder);
	}
	
	public void renameFolder(String id, String newName) {
		database.renameFolder(id, newName);
	}
	
	public void moveFolder(String id, String path) {
		database.moveFolder(id, path);
	}
	
	public void deleteFolder(String path) {
	    database.deleteFolder(path);
	}
	
	public Person getClientPerson() {
		return clientPerson;
	}
	
	public void editNote(long id, String newText) {
		database.editNote(id, newText);
	}
	
	public void renameNote(long id, String newName) {
		database.renameNote(id, newName);
	}
	
	public void moveNote(long id, String path) {
		database.moveNote(id, path);
	}
	
	public void deleteNote(long id) {
	    database.deleteNote(id);
	}
	
	@Override
	public void sync() {
		notifyListeners(ClientListener::sync);
	}
	
	@Override
	public void onLostConnection() {
		notifyListeners(ClientListener::onLostConnection);
	}
	
	public void addClientListener(ClientListener clientListener) {
        listeners.add(ClientListener.class, clientListener);
    }

    public void removeClientListener(ClientListener clientListener) {
        listeners.remove(ClientListener.class, clientListener);
    }

	private void notifyListeners(Consumer<ClientListener> action) {
		Arrays.stream(listeners.getListeners(ClientListener.class)).toList().forEach(action);
	}
}
