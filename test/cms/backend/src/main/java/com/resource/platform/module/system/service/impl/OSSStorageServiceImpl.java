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

import java.io.IOException;
import java.io.InputStream;

/**
 * 阿里云 OSS 存储服务实现
 *
 * <p>包含熔断降级保护：
 * <ul>
 *   <li>10次请求中失败率超过50%时触发熔断，打开30秒</li>
 *   <li>单次上传失败自动重试3次（指数退避）</li>
 *   <li>降级方案：自动切换到本地存储，保证业务连续性</li>
 * </ul>
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

    /** 降级方案：OSS 不可用时切换到本地存储 */
    @Autowired
    @Qualifier("localStorageService")
    private StorageService localStorageService;

    // ==================== upload(MultipartFile) ====================

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "uploadMultipartFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public String upload(MultipartFile file, String path) throws IOException {
        OSS ossClient = getOSSClient();
        try {
            String fileName = ImageUtil.generateFileName(file.getOriginalFilename());
            String objectName = path + "/" + fileName;
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, file.getInputStream()));
            log.info("文件上传 OSS 成功: {}", objectName);
            return getFileUrl(objectName);
        }
    }

    /** OSS 上传（MultipartFile）降级方法 */
    public String uploadMultipartFallback(MultipartFile file, String path, Throwable t) throws IOException {
        log.warn("OSS 上传失败，降级到本地存储: path={}, error={}", path, t.getMessage());
        return localStorageService.upload(file, path);
    }

    // ==================== upload(InputStream) ====================

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "uploadStreamFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public String upload(InputStream inputStream, String fileName, String path) throws IOException {
        OSS ossClient = getOSSClient();
        try {
            String newFileName = ImageUtil.generateFileName(fileName);
            String objectName = path + "/" + newFileName;
            ossClient.putObject(new PutObjectRequest(bucketName, objectName, inputStream));
            log.info("文件流上传 OSS 成功: {}", objectName);
            return getFileUrl(objectName);
        }
    }

    /** OSS 上传（InputStream）降级方法 */
    public String uploadStreamFallback(InputStream inputStream, String fileName, String path, Throwable t) throws IOException {
        log.warn("OSS 流上传失败，降级到本地存储: fileName={}, error={}", fileName, t.getMessage());
        return localStorageService.upload(inputStream, fileName, path);
    }

    // ==================== delete ====================

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "deleteFallback")
    public boolean delete(String fileUrl) {
        OSS ossClient = getOSSClient();
        try {
            String objectName = fileUrl.replace(urlPrefix + "/", "");
            ossClient.deleteObject(bucketName, objectName);
            log.info("从 OSS 删除文件成功: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error("从 OSS 删除文件失败: {}", e.getMessage());
            throw e;  // 抛出让熔断器感知
        }
    }

    /** OSS 删除降级方法（删除失败只记录日志，不影响业务主流程）*/
    public boolean deleteFallback(String fileUrl, Throwable t) {
        log.warn("OSS 删除熔断，跳过删除: fileUrl={}, error={}", fileUrl, t.getMessage());
        return false;
    }

    // ==================== exists ====================

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "existsFallback")
    public boolean exists(String filePath) {
        OSS ossClient = getOSSClient();
        try {
            return ossClient.doesObjectExist(bucketName, filePath);
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

    // ==================== 私有方法 ====================

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

    /**
     * 应用关闭时销毁 OSS 客户端，释放连接池资源
     */
    @javax.annotation.PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS 客户端已销毁");
        }
    }
}
