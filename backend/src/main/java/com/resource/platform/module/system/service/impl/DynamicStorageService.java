package com.resource.platform.module.system.service.impl;

import com.resource.platform.module.system.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Delegates every storage operation to the runtime-selected storage backend.
 */
@Service("dynamicStorageService")
public class DynamicStorageService implements StorageService {

    @Autowired
    private StorageServiceResolver storageServiceResolver;

    @Override
    public String upload(MultipartFile file, String path) throws IOException {
        return storageServiceResolver.getCurrentStorageService().upload(file, path);
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String path) throws IOException {
        return storageServiceResolver.getCurrentStorageService().upload(inputStream, fileName, path);
    }

    @Override
    public boolean delete(String fileUrl) {
        return storageServiceResolver.resolveStorageServiceByUrl(fileUrl).delete(fileUrl);
    }

    @Override
    public String getFileUrl(String filePath) {
        return storageServiceResolver.getCurrentStorageService().getFileUrl(filePath);
    }

    @Override
    public boolean exists(String filePath) {
        return storageServiceResolver.getCurrentStorageService().exists(filePath);
    }
}
