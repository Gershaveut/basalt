package dev.code_offline.basalt_server.repository;

import dev.code_offline.basalt_server.model.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Long> {
	Person findByUsername(String username);
}