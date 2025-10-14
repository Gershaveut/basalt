package dev.code_offline.basalt_server.repository;

import dev.code_offline.basalt_server.model.Folder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FolderRepository extends PagingAndSortingRepository<Folder, String>, CrudRepository<Folder, String> {
}