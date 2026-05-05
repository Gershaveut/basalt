package org.gershaveut.basalt_server.controller;

import org.gershaveut.basalt_server.repository.FileRepository;
import org.gershaveut.basalt_server.repository.FolderRepository;
import org.gershaveut.basalt_server.repository.PersonRepository;
import org.gershaveut.basalt_share.Util;
import org.gershaveut.basalt_share.model.File;
import org.gershaveut.basalt_share.model.Folder;
import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.CrudRepository;
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
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Secured({"ROLE_MEMBER"})
public abstract class AbstractFileController extends AbstractCurdController<File, Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileController.class);

    @Autowired
    FolderRepository folderRepository;
    @Autowired
    FileRepository fileRepository;
    @Autowired
    PersonRepository personRepository;
    
    @Override
    public ResponseEntity<File> addEntity(@AuthenticationPrincipal Person currentPerson, @RequestBody File entity) {
        var name = entity.getName();
        var path = entity.getPath();
        var number = 0;

        while (fileRepository.findByNameAndPath(name, path).isPresent()) {
            ++number;
            name = entity.getBaseName() + " " + number + entity.getExtension();
        }

        return super.addEntity(currentPerson, new File(name, currentPerson.getId(), entity.getRawContent(), path));
    }

    @Override
    @Secured({"ROLE_MEMBER"})
    public ResponseEntity<File> deleteEntity(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id) {
        var fileData = fileRepository.findById(id);

        if (fileData.isPresent()) {
            if (accessFile(currentPerson, fileData.get())) {
                return super.deleteEntity(currentPerson, id);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<File> rename(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newName) {
        var fileData = fileRepository.findById(id);
        
        if (fileData.isEmpty() || fileRepository.findByAbsolutePath(fileData.get().getPath() + "/" + newName).isPresent())
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        return updateFile(currentPerson, id, file -> file.setName(newName));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<File> move(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newPath) {
        return updateFile(currentPerson, id, file -> file.setPath(newPath));
    }

    @Secured({"ROLE_MODERATOR"})
    @PatchMapping("/{id}/author")
    public ResponseEntity<File> author(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody Long newAuthor) {
        if (!personRepository.existsById(newAuthor))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return updateFile(currentPerson, id, file -> file.setPerson(newAuthor));
    }

    @PatchMapping("/{id}/edit")
    public ResponseEntity<File> edit(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody(required = false) String newContent) {
        if (newContent == null) newContent = "";

        var finalContent = newContent;

        return updateFile(currentPerson, id, file -> file.setContent(finalContent));
    }

    private ResponseEntity<File> updateFile(Person currnetPerson, Long id, Consumer<File> updateAction) {
        var fileData = fileRepository.findById(id);

        if (fileData.isPresent()) {
            var file = fileData.get();

            if (accessFile(currnetPerson, file)) {
                updateAction.accept(file);
                fileRepository.save(file);

                sync();
                return new ResponseEntity<>(file, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured({"ROLE_MODERATOR"})
    @PostMapping("/import")
    public ResponseEntity<List<File>> importProject(@AuthenticationPrincipal Person currentPerson, @RequestParam("multipartFile") MultipartFile multipartFile) throws IOException {
        var zis = new ZipInputStream(multipartFile.getInputStream());
        var zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            var zipName = zipEntry.getName();

            if (zipEntry.isDirectory()) {
                folderRepository.save(new Folder(zipName.substring(0, zipName.length() - 1).replace("/", Folder.SEPARATOR)));
            } else {
                var content = zis.readAllBytes();
                File file;

                if (zipName.contains("/")) {
                    var splitAbsolutePath = Util.splitAbsolutePath(zipName, "/");
                    
                    file = new File(splitAbsolutePath.getFirst(), currentPerson.getId(), content, Folder.SEPARATOR + splitAbsolutePath.getSecond().replace("/", Folder.SEPARATOR));
                } else {
                    file = new File(zipName, currentPerson.getId(), content, null);
                }

                fileRepository.save(file);
            }

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

        folderRepository.findAll().forEach(folder -> {
            try {
                var zipEntry = new ZipEntry(folder.getPath().substring(1).replace(Folder.SEPARATOR, "/") + '/');
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
            } catch (Exception e) {
                LOGGER.error("Zip folder error", e);
            }
        });

        fileRepository.findAll().forEach(file -> {
            try {
                var zipEntry = new ZipEntry(file.getAbsolutePath().substring(1).replace(Folder.SEPARATOR, "/"));
                zipOut.putNextEntry(zipEntry);

                zipOut.write(file.getRawContent());
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
    
    private boolean accessFile(Person currnetPerson, File file) {
        return hasRole(currnetPerson, Role.MEMBER) && file.getPerson() == currnetPerson.getId() || hasRole(currnetPerson, Role.MODERATOR);
    }
    
    @Override
    protected CrudRepository<File, Long> getRepository() {
        return fileRepository;
    }
}
