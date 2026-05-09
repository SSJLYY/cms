package com.resource.platform.module.system.controller;

import com.resource.platform.module.system.service.impl.StorageSettingsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves local uploaded files from the runtime-configured storage path.
 */
@RestController
public class LocalUploadController {

    @Autowired
    private StorageSettingsProvider storageSettingsProvider;

    @GetMapping("/uploads/{*filePath}")
    public ResponseEntity<Resource> getUploadedFile(@PathVariable("filePath") String filePath) {
        Path rootPath = Paths.get(storageSettingsProvider.getLocalPath()).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(filePath).normalize();
        if (!targetPath.startsWith(rootPath)) {
            return ResponseEntity.notFound().build();
        }

        File file = targetPath.toFile();
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(file.getName())
            .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(new FileSystemResource(file));
    }
}
