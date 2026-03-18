package org.gershaveut.basalt.model.database;

import org.gershaveut.basalt_share.Util;
import org.gershaveut.basalt_share.model.Folder;
import org.gershaveut.basalt_share.model.Note;
import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import javax.swing.event.EventListenerList;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Database implements WebSocketHandler {
	private static final String NOTES = "/notes";
	private static final String PERSONS = "/persons";
	private static final String FOLDERS = "/folders";
	
	private static final int DEFAULT_PORT = 7600;
	
	private final EventListenerList listeners = new EventListenerList();
	private final WebClient webClient;
	
	private final Disposable session;
	
	public Database(String ip, String username, String password) throws ServerConnectException, NetworkVersionException, SSLException {
		if (!ip.contains(":"))
			ip = ip + ":" + DEFAULT_PORT;
		
		var httpClient = getHttpClient();
		
		this.webClient = WebClient.builder()
				.baseUrl("https://" + ip)
				.filter(ExchangeFilterFunctions.basicAuthentication(username, password))
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
		
		try {
			var version = Objects.requireNonNull(webClient.get()
					.retrieve()
					.bodyToMono(Byte.class)
					.block());
			
			if (version != Util.NETWORK_VERSION) {
				throw new NetworkVersionException();
			}
		} catch (NetworkVersionException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new ServerConnectException(exception.getMessage());
		}
		
		session = new ReactorNettyWebSocketClient(httpClient).execute(URI.create("wss://" + ip + "/echo"), this)
				.subscribe();
	}
	
	public static Boolean tryConnect(String ip) {
		try {
			if (!ip.contains(":"))
				ip = ip + ":" + DEFAULT_PORT;
			
			var webClient = WebClient.builder()
					.baseUrl("https://" + ip)
					.clientConnector(new ReactorClientHttpConnector(getHttpClient()))
					.build();
			
			webClient.head()
					.retrieve()
					.bodyToMono(Void.class)
					.block();
		} catch (Exception ignored) {
			return false;
		}
		
		return true;
	}
	
	private static HttpClient getHttpClient() throws SSLException {
		var sslContext = SslContextBuilder
				.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE)
				.build();
		return HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
	}
	
	public Database() throws ServerConnectException, NetworkVersionException, SSLException {
		this("localhost:" + DEFAULT_PORT, "admin", "12345");
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) {
		return session.receive()
				.doOnNext(webSocketMessage -> notifyListeners(DatabaseListener::sync))
				.doOnTerminate(() -> notifyListeners(DatabaseListener::onLostConnection))
				.then();
	}
	
	public void close() {
		session.dispose();
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
	
	private <T> void addEntity(String uri, T entity, Function<HttpStatusCode, Boolean> onError) {
		webClient.post()
				.uri(uri)
				.bodyValue(entity)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	private <T> void addEntity(String uri, T entity) {
		addEntity(uri, entity, httpStatusCode -> false);
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
	
	public Mono<Person> getClientPerson() {
		return webClient.get()
				.uri(PERSONS + "/current")
				.retrieve()
				.bodyToMono(Person.class);
	}
	
	public Mono<Person> getPerson(long id) {
		return getEntity(Person.class, PERSONS, id);
	}
	
	public Mono<Person> getPerson(String username) {
		return webClient.get()
				.uri(PERSONS + "/username" + "/" + username)
				.retrieve()
				.bodyToMono(Person.class);
	}
	
	public void addNote(Note note) {
		addEntity(NOTES, note);
	}
	
	public void addPerson(Person person, Function<HttpStatusCode, Boolean> onError) {
		addEntity(PERSONS + "/register", person, onError);
	}
	
	public void addFolder(Folder folder) {
		addEntity(FOLDERS, folder);
	}
	
	public void deleteNote(long id) {
		deleteEntity(NOTES, id);
	}
	
	public void deletePerson(long id, boolean deleteNotes) {
		webClient.delete()
				.uri(uriBuilder -> uriBuilder
						.path(PERSONS + "/" + id)
						.queryParam("deleteNotes", deleteNotes)
						.build())
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}

	public void deleteCurrentPerson(boolean deleteNotes) {
		webClient.delete()
				.uri(uriBuilder -> uriBuilder
						.path(PERSONS + "/current")
						.queryParam("deleteNotes", deleteNotes)
						.build())
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void deleteFolder(String path) {
		deleteEntity(FOLDERS, path);
	}

	public void renameClientPerson(String newName, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(PERSONS + "/rename")
				.bodyValue(newName)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	public void passwordClientPerson(String newPassword, String oldPassword) {
		webClient.patch()
				.uri(PERSONS + "/password")
				.bodyValue(newPassword)
				.header("oldPassword", oldPassword)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void descriptionClientPerson(String newDescription) {
		webClient.patch()
				.uri(PERSONS + "/description")
				.bodyValue(newDescription)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void rolePerson(long id, Role role) {
		webClient.patch()
				.uri(PERSONS + "/" + id + "/role")
				.bodyValue(role)
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void renameNote(long id, String newName, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(NOTES + "/" + id + "/rename")
				.bodyValue(newName)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
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
	
	public void authorNote(long id, long newAuthor, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(NOTES + "/" + id + "/author")
				.bodyValue(newAuthor)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	public void moveFolder(String id, String path, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(FOLDERS + "/" + id + "/move")
				.bodyValue(path)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	public void renameFolder(String id, String newName, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(FOLDERS + "/" + id + "/rename")
				.bodyValue(newName)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	private Function<ClientResponse, Mono<? extends Throwable>> handleError(Function<HttpStatusCode, Boolean> onError) {
		return clientResponse -> {
			if (onError.apply(clientResponse.statusCode())) {
				return Mono.empty();
			} else {
				return Mono.error(new WebClientResponseException(clientResponse.statusCode().value(), "", null, null, null));
			}
		};
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
}

