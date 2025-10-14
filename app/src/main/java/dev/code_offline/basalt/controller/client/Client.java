package dev.code_offline.basalt.controller.client;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;
import dev.code_offline.basalt.model.person.Role;
import reactor.core.publisher.Mono;

import javax.swing.event.EventListenerList;
import java.util.List;

public class Client {
    private final EventListenerList listeners = new EventListenerList();
    
    private final Database database;
    
    private final Folder root = new Folder(Folder.SEPARATOR);
    private final Person clientPerson = new Person("Вы", 0, Role.ADMIN, null);
    
    private boolean offline = false;
    
    public Client(Database database) {
        Main.logger.info("Initializing client...");
        
        this.database = database;
    }
    
    public Client() {
        this(new Database());
        
        this.offline = true;
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
	
	public void renameFolder(String path, String newName) {
        var newFolder = new Folder(path);
        newFolder.setName(newName);
        
        database.editFolder(path, newFolder);
	}
	
	public void moveFolder(String path, Folder folder) {
		var newFolder = new Folder(path);
		newFolder.setParent(folder);
		
		database.editFolder(path, newFolder);
	}
	
	public void deleteFolder(String path) {
	    database.deleteFolder(path);
	}
	
	public Person getClientPerson() {
		return clientPerson;
	}
	
	public void editNote(long id, String newText) {
        database.getNote(id).subscribe(n -> {
            n.setText(newText);
            
            database.editNote(id, n);
        });
	}
	
	public void renameNote(long id, String newName) {
        database.getNote(id).subscribe(n -> {
            n.setName(newName);
            
            database.editNote(id, n);
        });
	}
	
	public void moveNote(long id, Folder folder) {
		database.getNote(id).subscribe(n -> {
			n.setPath(folder.getPath());
			
			database.editNote(id, n);
		});
	}
	
	public void deleteNote(long id) {
	    database.deleteNote(id);
	}
	
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
