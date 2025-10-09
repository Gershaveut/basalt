package dev.code_offline.basalt_server;

import dev.code_offline.basalt_server.model.Folder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderRepository extends PagingAndSortingRepository<Folder, Long>, CrudRepository<Folder, Long> {
}