package com.resource.platform.module.system.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.resource.platform.module.system.service.StorageService;
import com.resource.platform.util.ImageUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;

/**
 * 阿里云 OSS 存储服务实现。
 */
@Slf4j
@Service("ossStorageService")
public class OSSStorageServiceImpl implements StorageService {

    private static final String CIRCUIT_BREAKER_NAME = "storage-service";

    private volatile OSS ossClient;

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

    @Autowired
    @Qualifier("localStorageService")
    private StorageService localStorageService;

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "uploadMultipartFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public String upload(MultipartFile file, String path) throws IOException {
        OSS client = getOSSClient();
        try (InputStream inputStream = file.getInputStream()) {
            String fileName = ImageUtil.generateFileName(file.getOriginalFilename());
            String objectName = path + "/" + fileName;
            client.putObject(new PutObjectRequest(bucketName, objectName, inputStream));
            log.info("文件上传 OSS 成功: {}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("文件上传到 OSS 失败: path={}, error={}", path, e.getMessage(), e);
            throw e;
        }
    }

    public String uploadMultipartFallback(MultipartFile file, String path, Throwable t) throws IOException {
        log.warn("OSS 上传失败，降级到本地存储: path={}, error={}", path, t.getMessage());
        return localStorageService.upload(file, path);
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "uploadStreamFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public String upload(InputStream inputStream, String fileName, String path) throws IOException {
        OSS client = getOSSClient();
        try {
            String newFileName = ImageUtil.generateFileName(fileName);
            String objectName = path + "/" + newFileName;
            client.putObject(new PutObjectRequest(bucketName, objectName, inputStream));
            log.info("文件流上传 OSS 成功: {}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("文件流上传到 OSS 失败: fileName={}, error={}", fileName, e.getMessage(), e);
            throw e;
        }
    }

    public String uploadStreamFallback(InputStream inputStream, String fileName, String path, Throwable t) throws IOException {
        log.warn("OSS 流上传失败，降级到本地存储: fileName={}, error={}", fileName, t.getMessage());
        return localStorageService.upload(inputStream, fileName, path);
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "deleteFallback")
    public boolean delete(String fileUrl) {
        OSS client = getOSSClient();
        try {
            String objectName = fileUrl.replace(urlPrefix + "/", "");
            client.deleteObject(bucketName, objectName);
            log.info("从 OSS 删除文件成功: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error("从 OSS 删除文件失败: fileUrl={}, error={}", fileUrl, e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteFallback(String fileUrl, Throwable t) {
        log.warn("OSS 删除熔断，跳过删除: fileUrl={}, error={}", fileUrl, t.getMessage());
        return false;
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "existsFallback")
    public boolean exists(String filePath) {
        OSS client = getOSSClient();
        try {
            return client.doesObjectExist(bucketName, filePath);
        } catch (Exception e) {
            log.error("OSS 文件存在性检查失败: filePath={}, error={}", filePath, e.getMessage(), e);
            throw e;
        }
    }

    public boolean existsFallback(String filePath, Throwable t) {
        log.warn("OSS 连通性检查熔断: filePath={}, error={}", filePath, t.getMessage());
        return false;
    }

    @Override
    public String getFileUrl(String filePath) {
        return urlPrefix + "/" + filePath;
    }

    private OSS getOSSClient() {
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                    log.info("OSS 客户端初始化完成: endpoint={}", endpoint);
                }
            }
        }
        return ossClient;
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS 客户端已销毁");
        }
    }
}
