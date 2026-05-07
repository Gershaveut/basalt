package org.gershaveut.basalt_server.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gershaveut.basalt_server.model.SFile;
import org.gershaveut.basalt_server.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

@Service
public class FileService {
    @Autowired
    FileRepository fileRepository;
   
    public void write(SFile file, byte[] content) throws IOException {
        FileUtils.writeByteArrayToFile(file.toFile(), content);
    }
    
    public byte[] read(SFile file) throws IOException {
        return FileUtils.readFileToByteArray(file.toFile());
    }
    
    public void delete(SFile file) throws IOException {
        FileUtils.delete(file.toFile());
    }
    
    public void rename(SFile file, String newName) throws IOException {
        FileUtils.moveFile(file.toFile(), new File(file.getPath() + SFile.SEPARATOR + newName));
    }
    
    public void move(SFile from, SFile to) throws IOException {
        var fromFile = from.toFile();
        var toFile = to.toFile();
        
        FileUtils.moveToDirectory(fromFile, toFile, false);
        
        FileUtils.listFilesAndDirs(fromFile, null, TrueFileFilter.INSTANCE).forEach(file -> {
            //fileRepository.save(new SFile(file.getName(), file.getPath(), ));
        });
    }
    
    private void updateFilesInDirectory(SFile dir, Consumer<SFile> updateAction) {
        var filesFile = FileUtils.listFilesAndDirs(dir.toFile(), null, TrueFileFilter.INSTANCE).stream().toList();
        var files = filesFile.stream().map(file -> fileRepository.findByAbsolutePath(file.getAbsolutePath()).orElseThrow()).toList();
        
        var map = new HashMap<File, SFile>();

        for (int i = 0; i < filesFile.size(); i++) {
            map.put(filesFile.get(i), files.get(i));
        }
        
        updateAction.accept(dir);
        
        
        map.get()
    }
    
    public List<SFile> listFilesAndDirectory(SFile dir) {
        return FileUtils.listFilesAndDirs(dir.toFile(), null, TrueFileFilter.INSTANCE).stream().map(file -> fileRepository.findByAbsolutePath(file.getAbsolutePath()).orElseThrow()).toList();
    }
}
