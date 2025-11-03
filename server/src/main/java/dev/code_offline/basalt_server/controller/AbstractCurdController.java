package dev.code_offline.basalt_server.controller;

import dev.code_offline.basalt_server.model.Person;
import dev.code_offline.basalt_server.websocket.BasaltSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCurdController<T, ID> {
	@Autowired
	BasaltSocketHandler basaltSocketHandler;
	
	@Secured({"ROLE_GUEST"})
	@GetMapping
	public ResponseEntity<List<T>> getEntities() {
		var entities = new ArrayList<T>();
		
		getRepository().findAll().forEach(entities::add);
		
		if (entities.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		return new ResponseEntity<>(entities, HttpStatus.OK);
	}
	
	@Secured({"ROLE_GUEST"})
	@GetMapping("/{id}")
	public ResponseEntity<T> getEntity(@PathVariable ID id) {
		var entityData = getRepository().findById(id);
		
		return entityData.map(t -> new ResponseEntity<>(t, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PostMapping
	public ResponseEntity<T> addEntity(@AuthenticationPrincipal Person currentPerson, @RequestBody T entity) {
		getRepository().save(entity);
		sync();
		return new ResponseEntity<>(entity, HttpStatus.CREATED);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<T> deleteEntity(@PathVariable ID id) {
		getRepository().deleteById(id);
		sync();
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	protected void sync() {
		basaltSocketHandler.sync();
	}
	
	protected abstract CrudRepository<T, ID> getRepository();
}
