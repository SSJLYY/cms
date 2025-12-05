package com.resource.platform.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.resource.platform.service.StorageService;
import com.resource.platform.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 阿里云OSS存储服务实现
 */
@Slf4j
@Service("ossStorageService")
public class OSSStorageServiceImpl implements StorageService {

    @Value("${storage.oss.endpoint:}")
    private String endpoint;

    @Value("${storage.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${storage.oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${storage.oss.bucket-name:}")
    private String bucketName;

    @Value("${storage.oss.url-prefix:}")
    private String urlPrefix;

    private OSS getOSSClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @Override
    public String upload(MultipartFile file, String path) throws IOException {
        OSS ossClient = getOSSClient();
        try {
            String fileName = ImageUtil.generateFileName(file.getOriginalFilename());
            String objectName = path + "/" + fileName;
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName, objectName, file.getInputStream());
            
            ossClient.putObject(putObjectRequest);
            log.info("文件上传到OSS成功: {}", objectName);
            
            return getFileUrl(objectName);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String path) throws IOException {
        OSS ossClient = getOSSClient();
        try {
            String newFileName = ImageUtil.generateFileName(fileName);
            String objectName = path + "/" + newFileName;
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName, objectName, inputStream);
            
            ossClient.putObject(putObjectRequest);
            log.info("文件上传到OSS成功: {}", objectName);
            
            return getFileUrl(objectName);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean delete(String fileUrl) {
        OSS ossClient = getOSSClient();
        try {
            String objectName = fileUrl.replace(urlPrefix + "/", "");
            ossClient.deleteObject(bucketName, objectName);
            log.info("从OSS删除文件成功: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error("从OSS删除文件失败: {}", e.getMessage());
            return false;
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        return urlPrefix + "/" + filePath;
    }

    @Override
    public boolean exists(String filePath) {
        OSS ossClient = getOSSClient();
        try {
            return ossClient.doesObjectExist(bucketName, filePath);
        } finally {
            ossClient.shutdown();
        }
    }
}
