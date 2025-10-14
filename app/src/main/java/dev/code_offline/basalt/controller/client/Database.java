package dev.code_offline.basalt.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

public class Database {
	private static final String NOTES = "/notes";
	private static final String PERSONS = "/persons";
	private static final String FOLDERS = "/folders";
	
	private static final int DEFAULT_PORT = 7600;
	
	private final WebClient webClient;
	
	public Database(String ip) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2HalModule());
		
		this.webClient = WebClient.builder()
				.baseUrl("http://" + ip)
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(cfg -> {
							cfg.defaultCodecs().jackson2JsonEncoder(
									new Jackson2JsonEncoder(mapper, MediaTypes.HAL_JSON));
							cfg.defaultCodecs().jackson2JsonDecoder(
									new Jackson2JsonDecoder(mapper, MediaTypes.HAL_JSON));
						})
						.build())
				.build();
	}
	
	public Database() {
		this("localhost:" + DEFAULT_PORT);
	}
	
	private <T> Mono<List<T>> getEntities(String uri) {
		return webClient.get()
				.uri(uri)
				.accept(MediaTypes.HAL_JSON)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<PagedModel<EntityModel<T>>>() {})
				.map(paged -> paged.getContent().stream()
						.map(EntityModel::getContent)
						.toList());
	}
	
	private <T> Mono<T> getEntity(String uri, long id) {
		return webClient.get()
				.uri(uri + "/" + id)
				.accept(MediaTypes.HAL_JSON)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<EntityModel<T>>() {})
				.map(EntityModel::getContent);
	}
	
	private <T> void addEntity(String uri, T entity) {
		webClient.post()
				.uri(uri)
				.bodyValue(entity)
				.retrieve();
	}

	private void deleteEntity(String uri, String id) {
		 webClient.delete()
				.uri(uri + "/" + id)
				.retrieve();
	}
	
	private void deleteEntity(String uri, long id) {
		deleteEntity(uri, String.valueOf(id));
	}
	
	private <T> void patchEntity(String uri, String id, T entity) {
		webClient.patch()
				.uri(uri + "/" + id)
				.bodyValue(entity)
				.retrieve();
	}
	
	private <T> void patchEntity(String uri, long id, T entity) {
		patchEntity(uri, String.valueOf(id), entity);
	}
	
	public Mono<List<Note>> getNotes() {
		return getEntities(NOTES);
	}
	
	public Mono<List<Person>> getPersons() {
		return getEntities(PERSONS);
	}
	
	public Mono<List<Folder>> getFolders() {
		return getEntities(FOLDERS);
	}
	
	public Mono<Note> getNote(long id) {
		return getEntity(NOTES, id);
	}
	
	public Mono<Person> getPerson(long id) {
		return getEntity(PERSONS, id);
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
	
	public void editNote(long id, Note note) {
		patchEntity(NOTES, id, note);
	}
	
	public void editPerson(long id, Person person) {
		patchEntity(PERSONS, id, person);
	}
	
	public void editFolder(String id, Folder folder) {
		patchEntity(FOLDERS, id, folder);
	}
}
