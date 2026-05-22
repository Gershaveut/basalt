package org.gershaveut.basalt_server.service;

import org.apache.commons.io.FileUtils;
import org.gershaveut.basalt_server.model.SFile;
import org.gershaveut.basalt_server.repository.FileRepository;
import org.gershaveut.basalt_share.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class FileService {
    @Autowired
    FileRepository fileRepository;
    
    public void write(SFile file, byte[] content) throws IOException {
        var file1 = file.toFile();
        
        if (file.isDirectory())
            FileUtils.forceMkdir(file1);
        else
            FileUtils.writeByteArrayToFile(file1, content);

        if (Util.isNote(file.getExtension())) {
            var links = new ArrayList<String>();

            var patternId = Pattern.compile("\\{(\\d*?)}");
            var patternName = Pattern.compile("\\[\\[(.*?)]]");

            var text = new String(read(file));

            var matcherId = patternId.matcher(text);
            var matcherName = patternName.matcher(text);

            while (matcherId.find()) {
                try {
                    var id = matcherId.group(1).trim();

                    if (Long.parseLong(id) != file.getId() && links.stream().noneMatch(l -> Objects.equals(l, id)))
                        links.add(id);
                } catch (Exception ignored) {
                }
            }

            while (matcherName.find()) {
                try {
                    var name = matcherName.group(1).trim();

                    if (!name.equals(file.getName()) && links.stream().noneMatch(l -> Objects.equals(l, name)))
                        links.add(name);
                } catch (Exception ignored) {
                }
            }

            file.setMetadata(Util.getMapper().writeValueAsString(links));
            fileRepository.save(file);
        }
    }
    
    public byte[] read(SFile file) throws IOException {
        return FileUtils.readFileToByteArray(file.toFile());
    }
    
    public void delete(SFile file) throws IOException {
        FileUtils.delete(file.toFile());
    }
    
    public void rename(SFile file, String newName) throws IOException {
        var destFile = new File(file.getFilePath() + newName);
        
        if (file.isDirectory())
            FileUtils.moveDirectory(file.toFile(), destFile);
        else
            FileUtils.moveFile(file.toFile(), destFile);
    }
    
    public void move(SFile from, SFile to) throws IOException {
        var fromFile = from.toFile();
        var toFile = to.toFile();
        
        FileUtils.moveToDirectory(fromFile, toFile, false);
    }
}
