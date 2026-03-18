package org.gershaveut.basalt_server.repository;

import org.gershaveut.basalt_share.model.Note;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoteRepository extends CrudRepository<Note, Long> {
	Note findByName(String name);
	@Query(value = "SELECT note_id FROM note_links WHERE links = ?1", nativeQuery = true)
	List<Long> findAllIdByLink(Long id);
}