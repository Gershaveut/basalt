package dev.code_offline.basalt_server.config;

import dev.code_offline.basalt_server.model.Folder;
import dev.code_offline.basalt_server.model.Note;
import dev.code_offline.basalt_server.model.Person;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RestConfig implements RepositoryRestConfigurer {
	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
		config.exposeIdsFor(Note.class);
		config.exposeIdsFor(Person.class);
		config.exposeIdsFor(Folder.class);
	}
}
