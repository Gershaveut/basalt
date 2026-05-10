package org.gershaveut.basalt_server.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gershaveut.basalt_server.model.SFile;
import org.gershaveut.basalt_server.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class FileService {
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
    }
}
