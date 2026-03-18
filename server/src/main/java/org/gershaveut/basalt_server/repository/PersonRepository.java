package org.gershaveut.basalt_server.repository;

import org.gershaveut.basalt_share.model.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Long> {
	Person findByUsername(String username);
}