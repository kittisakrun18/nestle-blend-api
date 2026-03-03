package com.nestle.blend.api.component;

import com.nestle.blend.api.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class FileComponent {

    @Value("${app.upload.path}")
    private String uploadPath;

    public File base64ToFile(String path, String base64Data, String fileName) throws IOException {
        String prefix = StringUtils.randomString(10);
        String fullPath = this.uploadPath + "/" + path;
        byte[] fileBytes = Base64.getDecoder().decode(base64Data);
        Path filePath = Paths.get(fullPath, prefix + "-" + fileName);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileBytes);

        return filePath.toFile();
    }

    public String fileToBase64(String filePath) throws IOException {
        Path path = Path.of(this.uploadPath + "/" + filePath);
        byte[] fileContent = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(fileContent);
    }

}
