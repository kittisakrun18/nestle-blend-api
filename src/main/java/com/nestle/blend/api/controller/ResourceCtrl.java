package com.nestle.blend.api.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@RestController
@RequestMapping(value = "${app.api.path.prefix}/resource")
public class ResourceCtrl {

    @Value("${app.upload.path}")
    private String uploadPath;

    private Path ROOT;

    @PostConstruct
    void init() {
        this.ROOT = Paths.get(uploadPath)
                .toAbsolutePath()
                .normalize();

        // optional: validate ตอน start
        if (!Files.exists(ROOT)) {
            throw new IllegalStateException("Upload root not found: " + ROOT);
        }
        if (!Files.isDirectory(ROOT)) {
            throw new IllegalStateException("Upload root is not a directory: " + ROOT);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(
            @RequestParam("file") String fileBase64
    ) {
        try {
            // 1️⃣ decode base64 -> relative path
            String relativePath = new String(
                    Base64.getDecoder().decode(fileBase64),
                    StandardCharsets.UTF_8
            );

            // 2️⃣ reject absolute path (Linux / Windows)
            if (relativePath.startsWith("/") || relativePath.matches("^[A-Za-z]:.*")) {
                return ResponseEntity
                        .badRequest()
                        .body("Absolute path is not allowed");
            }

            // normalize slash
            relativePath = relativePath.replace("\\", "/");

            // 3️⃣ ROOT + relative -> full path
            Path fullPath = ROOT
                    .resolve(relativePath)
                    .normalize();

            // 4️⃣ ❗ Security check: must be under ROOT
            if (!fullPath.startsWith(ROOT)) {
                return ResponseEntity
                        .status(403)
                        .body("Access denied");
            }

            // 5️⃣ check file exists
            if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
                return ResponseEntity
                        .notFound()
                        .build();
            }

            // 6️⃣ detect content-type
            String contentType = Files.probeContentType(fullPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 7️⃣ stream file
            InputStreamResource resource =
                    new InputStreamResource(Files.newInputStream(fullPath));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fullPath.getFileName() + "\""
                    )
                    .contentLength(Files.size(fullPath))
                    .body(resource);

        } catch (IllegalArgumentException e) {
            // base64 ผิด
            return ResponseEntity
                    .badRequest()
                    .body("Invalid base64");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .internalServerError()
                    .body("Cannot read file");
        }
    }
}
