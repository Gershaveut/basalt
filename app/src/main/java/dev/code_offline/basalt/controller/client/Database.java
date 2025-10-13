package dev.code_offline.basalt.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

public class Database {
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
	
	private <T> @Nullable List<T> getEntities(String uri) {
		return webClient.get()
				.uri(uri)
				.accept(MediaTypes.HAL_JSON)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<PagedModel<EntityModel<T>>>() {})
				.map(paged -> paged.getContent().stream()
						.map(EntityModel::getContent)
						.toList())
				.block();
	}
	
	public @Nullable List<Note> getNotes() {
		return getEntities("/notes");
	}
	
	public @Nullable List<Person> getPersons() {
		return getEntities("/persons");
	}
	
	public @Nullable List<Folder> getFolders() {
		return getEntities("/folders");
	}
}
