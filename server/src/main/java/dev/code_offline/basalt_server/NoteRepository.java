package dev.code_offline.basalt_server;

import dev.code_offline.basalt_server.model.Note;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends PagingAndSortingRepository<Note, Long>, CrudRepository<Note, Long> {
    List<Note> findByName(@Param("name") String name);
}