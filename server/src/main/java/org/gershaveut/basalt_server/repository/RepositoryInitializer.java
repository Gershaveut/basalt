package org.gershaveut.basalt_server.repository;

import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RepositoryInitializer {
	@Autowired
	PersonRepository personRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@EventListener(ApplicationReadyEvent.class)
	public void initAdmin() {
		if (personRepository.count() <= 0) {
			personRepository.save(new Person("admin", passwordEncoder.encode("12345"), Role.ADMIN, null));
		}
	}
}
