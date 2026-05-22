package org.gershaveut.basalt_server.controller;

import org.gershaveut.basalt_server.model.SFile;
import org.gershaveut.basalt_server.repository.CommentRepository;
import org.gershaveut.basalt_server.repository.FileRepository;
import org.gershaveut.basalt_server.repository.PersonRepository;
import org.gershaveut.basalt_server.service.FileService;
import org.gershaveut.basalt_share.Util;
import org.gershaveut.basalt_share.model.Comment;
import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
import org.gershaveut.basalt_share.model.SFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RestController
@Secured({"ROLE_MEMBER"})
@RequestMapping("/files")
public class FileController extends AbstractCurdController<SFile, Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    FileService fileService;

    @Autowired
    FileRepository fileRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    PersonRepository personRepository;

    @Override
    public ResponseEntity<SFile> addEntity(@AuthenticationPrincipal Person currentPerson, @RequestBody SFile entity) {
        var name = entity.getName();
        var path = entity.getPath();
        var isDirectory = entity.isDirectory();
        var number = 0;

        while (fileRepository.findByNameAndPath(name, path).isPresent()) {
            ++number;
            name = entity.getBaseName() + " " + number + entity.getExtension();
        }

        var file = new SFile(name, path, currentPerson);

        file.setDirectory(isDirectory);

        try {
            fileService.write(file, new byte[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return super.addEntity(currentPerson, file);
    }

    @Override
    @Secured({"ROLE_MEMBER"})
    public ResponseEntity<SFile> deleteEntity(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id) {
        var fileData = fileRepository.findById(id);

        if (fileData.isPresent()) {
            var file = fileData.get();

            if (accessFile(currentPerson, file)) {
                try {
                    fileService.delete(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return super.deleteEntity(currentPerson, id);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<SFile> rename(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newName) throws IOException {
        var fileData = fileRepository.findById(id);

        if (fileData.isEmpty() || fileRepository.findByAbsolutePath(fileData.get().getPath() + "/" + newName).isPresent())
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        fileService.rename(fileData.get(), newName);
        return updateFiles(currentPerson, id, file -> file.setName(newName));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<SFile> move(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody Long toId) throws IOException {
        var fromData = fileRepository.findById(id);
        var toData = fileRepository.findById(toId);

        if (fromData.isEmpty() || toData.isEmpty())
            return ResponseEntity.notFound().build();

        fileService.move(fromData.get(), toData.get());
        return updateFiles(currentPerson, id, file -> file.setPath(toData.get().getAbsolutePath()));
    }

    @Secured({"ROLE_MODERATOR"})
    @PatchMapping("/{id}/author")
    public ResponseEntity<SFile> author(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody Long newAuthor) {
        var personData = personRepository.findById(newAuthor);

        return personData.map(person -> updateFiles(currentPerson, id, file -> file.setPerson(person))).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    @Secured({"ROLE_GUEST"})
    @GetMapping("/{id}/read")
    public ResponseEntity<Resource> read(@PathVariable Long id) throws IOException {
        var fileData = fileRepository.findById(id);

        if (fileData.isEmpty())
            return ResponseEntity.notFound().build();

        var file = fileData.get();

        var resource = new ByteArrayResource(fileService.read(file));

        var mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        var headers = new HttpHeaders();
        headers.setContentType(mediaType);

        var contentDisposition = ContentDisposition
                .attachment()
                .filename(file.getName())
                .build();
        headers.setContentDisposition(contentDisposition);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @PatchMapping("/{id}/write")
    public ResponseEntity<SFile> write(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestParam("multipartFile") MultipartFile multipartFile) {
        return updateFiles(currentPerson, id, file -> {
            try {
                fileService.write(file, multipartFile.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Secured({"ROLE_MODERATOR"})
    @PostMapping("/import")
    public ResponseEntity<List<SFile>> importProject(@AuthenticationPrincipal Person currentPerson, @RequestParam("multipartFile") MultipartFile multipartFile) throws IOException {
        var zis = new ZipInputStream(multipartFile.getInputStream());
        var zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            var zipName = zipEntry.getName();

            var content = zis.readAllBytes();
            SFile file;

            if (zipName.contains("/")) {
                var splitAbsolutePath = Util.splitAbsolutePath(zipName, "/");

                file = new SFile(splitAbsolutePath.getFirst(), SFileHelper.SEPARATOR + splitAbsolutePath.getSecond(), currentPerson);
            } else {
                file = new SFile(zipName, currentPerson);
            }
            
            file.setDirectory(zipEntry.isDirectory());

            fileRepository.save(file);
            
            fileService.write(file, content);

            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

        sync();

        return getEntities();
    }

    @Secured({"ROLE_GUEST"})
    @GetMapping("/export")
    public ResponseEntity<Resource> exportProject() throws IOException {
        var fos = new ByteArrayOutputStream();
        var zipOut = new ZipOutputStream(fos);

        fileRepository.findAll().forEach(file -> {
            try {
                var zipEntry = new ZipEntry(file.getAbsolutePath().substring(1));
                zipOut.putNextEntry(zipEntry);

                zipOut.write(fileService.read(file));
            } catch (Exception e) {
                LOGGER.error("Zip file error", e);
            }
        });

        zipOut.close();
        fos.close();

        var resource = new ByteArrayResource(fos.toByteArray());

        var mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        var headers = new HttpHeaders();
        headers.setContentType(mediaType);

        var contentDisposition = ContentDisposition
                .attachment()
                .filename("export-" + new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) + ".zip")
                .build();
        headers.setContentDisposition(contentDisposition);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @Secured({"ROLE_GUEST"})
    @GetMapping("/{id}/comments")
    public ResponseEntity<PagedModel<Comment>> getComments(@PathVariable Long id, @PageableDefault Pageable pageable) {
        if (!fileRepository.existsById(id))
            return ResponseEntity.notFound().build();

        var commentsPage = commentRepository.findByFile(id, pageable);

        return ResponseEntity.ok(new PagedModel<>(commentsPage));
    }

    @Secured({"ROLE_GUEST"})
    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String text) {
        if (!fileRepository.existsById(id))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(commentRepository.save(new Comment(id, currentPerson, text)));
    }

    @Secured({"ROLE_GUEST"})
    @PostMapping("/{id}/comments/{commentId}/edit")
    public ResponseEntity<Comment> editComment(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @PathVariable Long commentId, @RequestBody String text) {
        if (!fileRepository.existsById(id))
            return ResponseEntity.notFound().build();

        var commentData = commentRepository.findById(commentId);

        if (commentData.isEmpty() || !accessComment(currentPerson, commentData.get()))
            return ResponseEntity.notFound().build();

        var comment = commentData.get();

        comment.setText(text);
        comment.setEdited(true);

        return ResponseEntity.ok(commentRepository.save(comment));
    }

    @Secured({"ROLE_GUEST"})
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Comment> deleteComment(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @PathVariable Long commentId) {
        if (!fileRepository.existsById(id))
            return ResponseEntity.notFound().build();

        var commentData = commentRepository.findById(commentId);

        if (commentData.isEmpty() || !accessComment(currentPerson, commentData.get()))
            return ResponseEntity.notFound().build();

        commentRepository.deleteById(commentId);

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<SFile> updateFiles(Person currnetPerson, Long id, Consumer<SFile> updateAction) {
        var fileData = fileRepository.findById(id);

        if (fileData.isEmpty())
            return ResponseEntity.notFound().build();

        var file = fileData.get();

        if (!accessFile(currnetPerson, file))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        var oldDirPath = file.getPath();

        updateAction.accept(file);
        fileRepository.save(file);

        fileRepository.findAll().forEach(file1 -> {
            if (Objects.equals(file1.getParent(), oldDirPath)) {
                if (file1.isDirectory())
                    updateFiles(currnetPerson, file1.getId(), f -> f.setPath(file.getPath()));
                else {
                    file1.setPath(file.getPath());
                    fileRepository.save(file1);
                }
            }
        });

        sync();
        
        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    private boolean accessComment(Person currnetPerson, Comment comment) {
        return comment.getPerson().getId() == currnetPerson.getId() || hasRole(currnetPerson, Role.MODERATOR);
    }

    private boolean accessFile(Person currnetPerson, SFile file) {
        return hasRole(currnetPerson, Role.MEMBER) && file.getPerson().getId() == currnetPerson.getId() || hasRole(currnetPerson, Role.MODERATOR);
    }

    @Override
    protected CrudRepository<SFile, Long> getRepository() {
        return fileRepository;
    }
}
