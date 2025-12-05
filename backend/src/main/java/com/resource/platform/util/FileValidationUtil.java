package com.resource.platform.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 文件上传验证工具类
 * 提供文件类型验证、文件大小验证、XSS防护等功能
 */
@Slf4j
public class FileValidationUtil {

    // 默认允许的图片类型
    private static final Set<String> DEFAULT_ALLOWED_IMAGE_TYPES = new HashSet<>(
        Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp")
    );

    // 默认最大文件大小 (10MB)
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    // XSS危险字符模式
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script|javascript:|onerror=|onload=|<iframe|<object|<embed",
        Pattern.CASE_INSENSITIVE
    );

    // 文件名危险字符模式
    private static final Pattern DANGEROUS_FILENAME_PATTERN = Pattern.compile(
        "[<>:\"/\\\\|?*\\x00-\\x1f]"
    );

    /**
     * 验证文件类型
     * 
     * @param file 上传的文件
     * @param allowedTypes 允许的文件类型集合
     * @return 是否通过验证
     */
    public static boolean validateFileType(MultipartFile file, Set<String> allowedTypes) {
        if (file == null || file.isEmpty()) {
            log.warn("文件为空");
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            log.warn("文件名为空");
            return false;
        }

        String extension = ImageUtil.getFileExtension(originalFilename);
        if (extension.isEmpty()) {
            log.warn("文件没有扩展名");
            return false;
        }

        // 移除点号并转为小写
        extension = extension.substring(1).toLowerCase();

        if (!allowedTypes.contains(extension)) {
            log.warn("不允许的文件类型: {}", extension);
            return false;
        }

        return true;
    }

    /**
     * 验证图片文件类型（使用默认允许的类型）
     */
    public static boolean validateImageType(MultipartFile file) {
        return validateFileType(file, DEFAULT_ALLOWED_IMAGE_TYPES);
    }

    /**
     * 验证文件大小
     * 
     * @param file 上传的文件
     * @param maxSize 最大文件大小（字节）
     * @return 是否通过验证
     */
    public static boolean validateFileSize(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            log.warn("文件为空");
            return false;
        }

        long fileSize = file.getSize();
        if (fileSize > maxSize) {
            log.warn("文件大小超过限制: {} > {}", fileSize, maxSize);
            return false;
        }

        return true;
    }

    /**
     * 验证文件大小（使用默认最大大小）
     */
    public static boolean validateFileSize(MultipartFile file) {
        return validateFileSize(file, DEFAULT_MAX_FILE_SIZE);
    }

    /**
     * 验证文件名安全性（防止路径遍历和XSS）
     * 
     * @param filename 文件名
     * @return 是否安全
     */
    public static boolean validateFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            log.warn("文件名为空");
            return false;
        }

        // 检查路径遍历攻击
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            log.warn("文件名包含危险字符（路径遍历）: {}", filename);
            return false;
        }

        // 检查危险字符
        if (DANGEROUS_FILENAME_PATTERN.matcher(filename).find()) {
            log.warn("文件名包含危险字符: {}", filename);
            return false;
        }

        // 检查XSS攻击
        if (XSS_PATTERN.matcher(filename).find()) {
            log.warn("文件名包含XSS攻击代码: {}", filename);
            return false;
        }

        return true;
    }

    /**
     * 清理文件名（移除危险字符）
     * 
     * @param filename 原始文件名
     * @return 清理后的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        // 移除路径分隔符
        filename = filename.replace("/", "").replace("\\", "");

        // 移除危险字符
        filename = DANGEROUS_FILENAME_PATTERN.matcher(filename).replaceAll("_");

        // 移除XSS相关内容
        filename = XSS_PATTERN.matcher(filename).replaceAll("");

        return filename;
    }

    /**
     * 综合验证文件
     * 
     * @param file 上传的文件
     * @param allowedTypes 允许的文件类型
     * @param maxSize 最大文件大小
     * @return 验证结果消息，null表示验证通过
     */
    public static String validateFile(MultipartFile file, Set<String> allowedTypes, long maxSize) {
        if (file == null || file.isEmpty()) {
            return "文件为空";
        }

        // 验证文件名
        String originalFilename = file.getOriginalFilename();
        if (!validateFilename(originalFilename)) {
            return "文件名包含非法字符";
        }

        // 验证文件类型
        if (!validateFileType(file, allowedTypes)) {
            return "不支持的文件类型";
        }

        // 验证文件大小
        if (!validateFileSize(file, maxSize)) {
            return "文件大小超过限制";
        }

        return null;
    }

    /**
     * 综合验证图片文件（使用默认配置）
     */
    public static String validateImageFile(MultipartFile file) {
        return validateFile(file, DEFAULT_ALLOWED_IMAGE_TYPES, DEFAULT_MAX_FILE_SIZE);
    }

    /**
     * 验证内容是否包含XSS攻击代码
     * 
     * @param content 内容
     * @return 是否包含XSS代码
     */
    public static boolean containsXSS(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return XSS_PATTERN.matcher(content).find();
    }

    /**
     * 清理XSS攻击代码
     * 
     * @param content 原始内容
     * @return 清理后的内容
     */
    public static String sanitizeXSS(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // 移除script标签
        content = content.replaceAll("(?i)<script.*?>.*?</script>", "");
        
        // 移除javascript:
        content = content.replaceAll("(?i)javascript:", "");
        
        // 移除事件处理器
        content = content.replaceAll("(?i)on\\w+\\s*=", "");
        
        // 移除iframe、object、embed标签
        content = content.replaceAll("(?i)<(iframe|object|embed).*?>.*?</\\1>", "");
        
        return content;
    }

    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
