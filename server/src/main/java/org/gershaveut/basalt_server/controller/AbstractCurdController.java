package org.gershaveut.basalt_server.controller;

import org.gershaveut.basalt_server.websocket.SpringApplicationSocketHandler;
import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Secured({"ROLE_GUEST"})
public abstract class AbstractCurdController<T, ID> {
	@Autowired
	SpringApplicationSocketHandler springApplicationSocketHandler;
	@Autowired
	RoleHierarchy roleHierarchy;
	
	@GetMapping
	public ResponseEntity<List<T>> getEntities() {
		var entities = new ArrayList<T>();
		
		getRepository().findAll().forEach(entities::add);
		
		if (entities.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		return new ResponseEntity<>(entities, HttpStatus.OK);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<T> getEntity(@PathVariable ID id) {
		var entityData = getRepository().findById(id);
		
		return entityData.map(t -> new ResponseEntity<>(t, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@Secured({"ROLE_MEMBER"})
	@PostMapping
	public ResponseEntity<T> addEntity(@AuthenticationPrincipal Person currentPerson, @RequestBody T entity) {
		getRepository().save(entity);
		sync();
		return new ResponseEntity<>(entity, HttpStatus.CREATED);
	}
	
	@Secured({"ROLE_MODERATOR"})
	@DeleteMapping("/{id}")
	public ResponseEntity<T> deleteEntity(@AuthenticationPrincipal Person currentPerson, @PathVariable ID id) {
		getRepository().deleteById(id);
		sync();
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	protected boolean hasRole(Person person, Role role) {
		return roleHierarchy.getReachableGrantedAuthorities(person.getAuthorities()).contains(role.getGrantedAuthority());
	}
	
	protected void sync() {
		springApplicationSocketHandler.sync();
	}
	
	protected abstract CrudRepository<T, ID> getRepository();
}
