package dev.code_offline.basalt.model.database;

import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import reactor.core.publisher.Mono;

import javax.swing.event.EventListenerList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Database implements WebSocketHandler {
	private static final String NOTES = "/notes";
	private static final String PERSONS = "/persons";
	private static final String FOLDERS = "/folders";
	
	private static final int DEFAULT_PORT = 7600;
	
	private final EventListenerList listeners = new EventListenerList();
	private final WebClient webClient;
	private final CompletableFuture<WebSocketSession> session;
	
	public Database(String ip) throws Exception {
		if (!ip.contains(":"))
			ip = ip + ":" + DEFAULT_PORT;
		
		this.webClient = WebClient.create("http://" + ip);

		if (webClient.head().exchangeToMono(clientResponse -> Mono.just(clientResponse.statusCode())).block() != HttpStatus.NO_CONTENT) {
			throw new Exception("Server connect error");
		}
		
		session = new StandardWebSocketClient().execute(this, "ws://" + ip + "/echo");
	}
	
	public Database() throws Exception {
		this("localhost:" + DEFAULT_PORT);
	}
	
	public void close() {
		try {
			session.get().close();
		} catch (Exception ignored) {
		}
	}
	
	private <T> Mono<List<T>> getEntities(Class<T> type, String uri) {
		return webClient.get()
				.uri(uri)
				.retrieve()
				.bodyToFlux(type)
				.collectList();
	}
	
	private <T> Mono<T> getEntity(Class<T> type, String uri, long id) {
		return webClient.get()
				.uri(uri + "/" + id)
				.retrieve()
				.bodyToMono(type);
	}
	
	private <T> void addEntity(String uri, T entity) {
		webClient.post()
				.uri(uri)
				.bodyValue(entity)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}

	private void deleteEntity(String uri, String id) {
		 webClient.delete()
				.uri(uri + "/" + id)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	private void deleteEntity(String uri, long id) {
		deleteEntity(uri, String.valueOf(id));
	}
	
	public Mono<List<Note>> getNotes() {
		return getEntities(Note.class, NOTES);
	}
	
	public Mono<List<Person>> getPersons() {
		return getEntities(Person.class, PERSONS);
	}
	
	public Mono<List<Folder>> getFolders() {
		return getEntities(Folder.class, FOLDERS);
	}
	
	public Mono<Note> getNote(long id) {
		return getEntity(Note.class, NOTES, id);
	}
	
	public Mono<Person> getPerson(long id) {
		return getEntity(Person.class, PERSONS, id);
	}
	
	public void addNote(Note note) {
		addEntity(NOTES, note);
	}
	
	public void addPerson(Person person) {
		addEntity(PERSONS, person);
	}
	
	public void addFolder(Folder folder) {
		addEntity(FOLDERS, folder);
	}
	
	public void deleteNote(long id) {
		deleteEntity(NOTES, id);
	}
	
	public void deletePerson(long id) {
		deleteEntity(PERSONS, id);
	}
	
	public void deleteFolder(String path) {
		deleteEntity(FOLDERS, path);
	}
	
	public void renameNote(long id, String newName) {
		webClient.patch()
				.uri(NOTES + "/" + id + "/rename")
				.bodyValue(newName)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void editNote(long id, String newText) {
		webClient.patch()
				.uri(NOTES + "/" + id + "/edit")
				.bodyValue(newText)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void moveNote(long id, String path) {
		webClient.patch()
				.uri(NOTES + "/" + id + "/move")
				.bodyValue(path)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void moveFolder(String id, String path) {
		webClient.patch()
				.uri(FOLDERS + "/" + id + "/move")
				.bodyValue(path)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void renameFolder(String id, String newName) {
		webClient.patch()
				.uri(FOLDERS + "/" + id + "/rename")
				.bodyValue(newName)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void addDatabaseListener(DatabaseListener clientListener) {
		listeners.add(DatabaseListener.class, clientListener);
	}
	
	public void removeDatabaseListener(DatabaseListener clientListener) {
		listeners.remove(DatabaseListener.class, clientListener);
	}
	
	private void notifyListeners(Consumer<DatabaseListener> action) {
		Arrays.stream(listeners.getListeners(DatabaseListener.class)).toList().forEach(action);
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
	
	}
	
	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
		notifyListeners(DatabaseListener::sync);
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
	
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
		if (closeStatus.getCode() == CloseStatus.NO_CLOSE_FRAME.getCode()) {
			notifyListeners(DatabaseListener::onLostConnection);
		}
	}
	
	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}

