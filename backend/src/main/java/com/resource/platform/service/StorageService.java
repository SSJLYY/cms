package com.resource.platform.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 存储服务接口
 * 支持多种存储方式：本地、OSS、COS、七牛云
 */
public interface StorageService {

    /**
     * 上传文件
     * 
     * @param file 文件
     * @param path 存储路径
     * @return 文件访问URL
     */
    String upload(MultipartFile file, String path) throws IOException;

    /**
     * 上传文件（从输入流）
     * 
     * @param inputStream 输入流
     * @param fileName 文件名
     * @param path 存储路径
     * @return 文件访问URL
     */
    String upload(InputStream inputStream, String fileName, String path) throws IOException;

    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean delete(String fileUrl);

    /**
     * 获取文件访问URL
     * 
     * @param filePath 文件路径
     * @return 访问URL
     */
    String getFileUrl(String filePath);

    /**
     * 检查文件是否存在
     * 
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean exists(String filePath);
}
