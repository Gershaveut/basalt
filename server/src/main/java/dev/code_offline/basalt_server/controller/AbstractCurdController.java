package dev.code_offline.basalt_server.controller;

import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCurdController<T, ID> {
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

	@PostMapping
	public ResponseEntity<T> addEntity(@RequestBody T entity) {
		getRepository().save(entity);
		return new ResponseEntity<>(entity, HttpStatus.CREATED);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<T> deleteEntity(@PathVariable ID id) {
		getRepository().deleteById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	protected abstract CrudRepository<T, ID> getRepository();
}
