package dev.code_offline.basalt_server.repository;

import dev.code_offline.basalt_server.model.Note;
import org.springframework.data.repository.CrudRepository;

public interface NoteRepository extends CrudRepository<Note, Long> {
}