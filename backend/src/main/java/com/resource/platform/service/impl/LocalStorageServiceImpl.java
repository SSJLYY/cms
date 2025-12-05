package com.resource.platform.service.impl;

import com.resource.platform.service.StorageService;
import com.resource.platform.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地存储服务实现
 */
@Slf4j
@Service("localStorageService")
public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.local.path:/data/uploads}")
    private String uploadPath;

    @Value("${storage.local.url-prefix:http://localhost:8080/uploads}")
    private String urlPrefix;

    @Override
    public String upload(MultipartFile file, String path) throws IOException {
        // 构建完整路径
        String fullPath = uploadPath + "/" + path;
        Path directory = Paths.get(fullPath);
        
        // 确保目录存在
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("创建目录: {}", directory.toString());
        }

        // 生成文件名
        String fileName = ImageUtil.generateFileName(file.getOriginalFilename());
        Path filePath = directory.resolve(fileName);

        // 使用输入流复制文件，避免 transferTo 的临时目录问题
        try (InputStream inputStream = file.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
            IOUtils.copy(inputStream, outputStream);
            log.info("文件上传成功: {}", filePath.toString());
        } catch (IOException e) {
            log.error("文件上传失败: {}", filePath.toString(), e);
            throw e;
        }

        // 返回访问URL
        return getFileUrl(path + "/" + fileName);
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String path) throws IOException {
        // 构建完整路径
        String fullPath = uploadPath + "/" + path;
        Path directory = Paths.get(fullPath);
        
        // 确保目录存在
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // 生成文件名
        String newFileName = ImageUtil.generateFileName(fileName);
        Path filePath = directory.resolve(newFileName);

        // 保存文件
        try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
            IOUtils.copy(inputStream, outputStream);
        }
        log.info("文件上传成功: {}", filePath.toString());

        // 返回访问URL
        return getFileUrl(path + "/" + newFileName);
    }

    @Override
    public boolean delete(String fileUrl) {
        try {
            // 从URL中提取文件路径
            String filePath = fileUrl.replace(urlPrefix, uploadPath);
            return ImageUtil.deleteLocalFile(filePath);
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        // 确保 urlPrefix 不以斜杠结尾
        String prefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
        // 确保 filePath 以斜杠开头
        String path = filePath.startsWith("/") ? filePath : "/" + filePath;
        return prefix + path;
    }

    @Override
    public boolean exists(String filePath) {
        String fullPath = uploadPath + "/" + filePath;
        return Files.exists(Paths.get(fullPath));
    }
}
