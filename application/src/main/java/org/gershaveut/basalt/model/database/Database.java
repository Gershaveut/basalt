package org.gershaveut.basalt.model.database;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.gershaveut.basalt.model.file.SFile;
import org.gershaveut.basalt_share.Util;
import org.gershaveut.basalt_share.model.Comment;
import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import javax.swing.event.EventListenerList;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Database implements WebSocketHandler {
	private static final String FILES = "/files";
	private static final String PERSONS = "/persons";
	private static final String COMMENTS = "/comments";
	
	private static final int DEFAULT_PORT = 7600;
	
	private final EventListenerList listeners = new EventListenerList();
	private final WebClient webClient;
	
	private final Disposable session;

	private int commentsSize = 20; 
	
	public Database(String ip, String username, String password) throws ServerConnectException, NetworkVersionException, SSLException {
		if (!ip.contains(":"))
			ip = ip + ":" + DEFAULT_PORT;
		
		var httpClient = getHttpClient();

		var size = (int) DataSize.ofGigabytes(1).toBytes();
		var strategies = ExchangeStrategies.builder()
				.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
				.build();
		
		this.webClient = WebClient.builder()
				.baseUrl("https://" + ip)
				.filter(ExchangeFilterFunctions.basicAuthentication(username, password))
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.exchangeStrategies(strategies)
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
	
	public Mono<List<SFile>> getFiles() {
		return getEntities(SFile.class, FILES);
	}
	
	public Mono<List<Person>> getPersons() {
		return getEntities(Person.class, PERSONS);
	}
	
	public Mono<SFile> getFile(long id) {
		return getEntity(SFile.class, FILES, id);
	}
	
	public Mono<Resource> readFile(long id) {
		return webClient.get()
				.uri(FILES + "/" + id + "/read")
				.retrieve()
				.bodyToMono(Resource.class);
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
	
	public void addFile(SFile file) {
		addEntity(FILES, file);
	}
	
	public void addPerson(Person person, Function<HttpStatusCode, Boolean> onError) {
		addEntity(PERSONS + "/register", person, onError);
	}
	
	public void deleteFile(long id) {
		deleteEntity(FILES, id);
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
	
	public void renameClientPerson(String newName, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(PERSONS + "/rename")
				.bodyValue(newName)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	public void passwordClientPerson(String newPassword, String oldPassword, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(PERSONS + "/password")
				.bodyValue(newPassword)
				.header("oldPassword", oldPassword)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
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
	
	public void renameFile(long id, String newName, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(FILES + "/" + id + "/rename")
				.bodyValue(newName)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	public void writeFile(long id, byte[] content) {
		var builder = new MultipartBodyBuilder();
		builder.part("file", new ByteArrayResource(content));

		webClient.patch()
				.uri(FILES + "/" + id + "/write")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(BodyInserters.fromMultipartData(builder.build()))
				.retrieve()
				.toBodilessEntity()
				.subscribe();
	}
	
	public void moveFile(long id, long toId, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(FILES + "/" + id + "/move")
				.bodyValue(toId)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	public void authorFile(long id, long newAuthor, Function<HttpStatusCode, Boolean> onError) {
		webClient.patch()
				.uri(FILES + "/" + id + "/author")
				.bodyValue(newAuthor)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	public void importProject(File file, Function<HttpStatusCode, Boolean> onError) {
		var builder = new MultipartBodyBuilder();
		builder.part("file", new FileSystemResource(file));
		
		webClient.post()
				.uri(FILES + "/import")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(BodyInserters.fromMultipartData(builder.build()))
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.toBodilessEntity()
				.subscribe();
	}
	
	public Mono<Resource> exportProject() {
		return webClient.get()
				.uri(FILES + "/export")
				.retrieve()
				.bodyToMono(Resource.class);
	}
	
	public Mono<PagedModel<Comment>> getComments(long id, long page) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(FILES + "/{id}" + COMMENTS)
						.queryParam("page", page)
						.queryParam("size", commentsSize)
						.build(id))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<JacksonPageModel<Comment>>() {})
				.map(commentJacksonPageModel -> commentJacksonPageModel);
	}
	
	public void addComment(long id, String text, Function<HttpStatusCode, Boolean> onError, Function<HttpStatusCode, Boolean> onSuccessful) {
		webClient.post()
				.uri(FILES + "/" + id + COMMENTS)
				.bodyValue(text)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.onStatus(HttpStatusCode::is2xxSuccessful, handleError(onSuccessful))
				.toBodilessEntity()
				.subscribe();
	}

	public void editComment(long id, long commentId, String text, Function<HttpStatusCode, Boolean> onError, Function<HttpStatusCode, Boolean> onSuccessful) {
		webClient.post()
				.uri(FILES + "/" + id + COMMENTS + "/" + commentId + "/edit")
				.bodyValue(text)
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.onStatus(HttpStatusCode::is2xxSuccessful, handleError(onSuccessful))
				.toBodilessEntity()
				.subscribe();
	}
	
	public void deleteComment(long id, long commentId, Function<HttpStatusCode, Boolean> onError, Function<HttpStatusCode, Boolean> onSuccessful) {
		webClient.delete()
				.uri(uriBuilder -> uriBuilder
						.path(FILES + "/{id}" + COMMENTS + "/{commentId}")
						.build(id, commentId))
				.retrieve()
				.onStatus(HttpStatusCode::isError, handleError(onError))
				.onStatus(HttpStatusCode::is2xxSuccessful, handleError(onSuccessful))
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

	public void setCommentsSize(int commentsSize) {
		this.commentsSize = commentsSize;
	}
}

