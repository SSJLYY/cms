package com.resource.platform.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 图片处理工具类
 * 提供图片上传、缩略图生成、图片压缩等功能
 */
@Slf4j
public class ImageUtil {

    /**
     * 生成唯一文件名
     */
    public static String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 获取图片尺寸
     */
    public static int[] getImageDimensions(File file) throws IOException {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                // 对于不支持的格式（如WebP），返回默认尺寸
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".webp") || fileName.endsWith(".avif") || fileName.endsWith(".heic")) {
                    return new int[]{800, 600}; // 默认尺寸
                }
                throw new IOException("无法读取图片文件");
            }
            return new int[]{image.getWidth(), image.getHeight()};
        } catch (Exception e) {
            // 对于任何读取异常，检查是否是已知的不支持格式
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".webp") || fileName.endsWith(".avif") || fileName.endsWith(".heic")) {
                return new int[]{800, 600}; // 默认尺寸
            }
            throw new IOException("无法读取图片文件: " + e.getMessage());
        }
    }

    /**
     * 获取图片尺寸（从MultipartFile）
     */
    public static int[] getImageDimensions(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        
        // 先检查是否是不支持的格式
        if (fileName != null) {
            String lowerFileName = fileName.toLowerCase();
            if (lowerFileName.endsWith(".webp") || lowerFileName.endsWith(".avif") || lowerFileName.endsWith(".heic")) {
                log.info("检测到不支持的图片格式，使用默认尺寸: {}", fileName);
                return new int[]{800, 600}; // 默认尺寸
            }
        }
        
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                log.warn("无法读取图片文件，使用默认尺寸: {}", fileName);
                return new int[]{800, 600}; // 默认尺寸
            }
            return new int[]{image.getWidth(), image.getHeight()};
        } catch (Exception e) {
            log.warn("读取图片尺寸失败，使用默认尺寸: {}, error: {}", fileName, e.getMessage());
            return new int[]{800, 600}; // 默认尺寸
        }
    }

    /**
     * 生成缩略图
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param width 目标宽度
     * @param height 目标高度
     * @param keepAspectRatio 是否保持宽高比
     */
    public static void generateThumbnail(File sourceFile, File targetFile, 
                                        int width, int height, boolean keepAspectRatio) throws IOException {
        try {
            // 检查是否是不支持的格式
            String fileName = sourceFile.getName().toLowerCase();
            if (fileName.endsWith(".webp") || fileName.endsWith(".avif") || fileName.endsWith(".heic")) {
                // 对于不支持的格式，直接复制原文件作为缩略图
                java.nio.file.Files.copy(sourceFile.toPath(), targetFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.info("不支持的图片格式，直接复制原文件作为缩略图: {}", targetFile.getAbsolutePath());
                return;
            }
            
            if (keepAspectRatio) {
                Thumbnails.of(sourceFile)
                    .size(width, height)
                    .toFile(targetFile);
            } else {
                Thumbnails.of(sourceFile)
                    .forceSize(width, height)
                    .toFile(targetFile);
            }
            log.info("缩略图生成成功: {}", targetFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("生成缩略图失败: {}", e.getMessage());
            // 如果缩略图生成失败，尝试直接复制原文件
            try {
                java.nio.file.Files.copy(sourceFile.toPath(), targetFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.info("缩略图生成失败，使用原文件: {}", targetFile.getAbsolutePath());
            } catch (IOException copyError) {
                log.error("复制原文件也失败: {}", copyError.getMessage());
                throw e;
            }
        }
    }

    /**
     * 生成缩略图（从字节数组）
     */
    public static byte[] generateThumbnail(byte[] imageBytes, int width, int height, 
                                          boolean keepAspectRatio) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            if (keepAspectRatio) {
                Thumbnails.of(inputStream)
                    .size(width, height)
                    .toOutputStream(outputStream);
            } else {
                Thumbnails.of(inputStream)
                    .forceSize(width, height)
                    .toOutputStream(outputStream);
            }
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.warn("生成缩略图失败，返回原图片数据: {}", e.getMessage());
            // 对于不支持的格式（如WebP），直接返回原图片数据
            return imageBytes;
        }
    }

    /**
     * 压缩图片
     * 
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param quality 压缩质量 (0.0-1.0)
     */
    public static void compressImage(File sourceFile, File targetFile, double quality) throws IOException {
        try {
            Thumbnails.of(sourceFile)
                .scale(1.0)
                .outputQuality(quality)
                .toFile(targetFile);
            log.info("图片压缩成功: {}, 质量: {}", targetFile.getAbsolutePath(), quality);
        } catch (IOException e) {
            log.error("压缩图片失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 压缩图片（从字节数组）
     */
    public static byte[] compressImage(byte[] imageBytes, double quality) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Thumbnails.of(inputStream)
                .scale(1.0)
                .outputQuality(quality)
                .toOutputStream(outputStream);
            
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("压缩图片失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 保存上传的文件到本地
     */
    public static String saveToLocal(MultipartFile file, String uploadDir) throws IOException {
        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String fileName = generateFileName(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(fileName);

        // 保存文件
        file.transferTo(filePath.toFile());
        log.info("文件保存成功: {}", filePath.toString());

        return fileName;
    }

    /**
     * 删除本地文件
     */
    public static boolean deleteLocalFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", filePath);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证是否为图片文件
     */
    public static boolean isImageFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg") || 
               extension.equals(".png") || extension.equals(".gif") || 
               extension.equals(".bmp") || extension.equals(".webp") ||
               extension.equals(".avif") || extension.equals(".heic");
    }

    /**
     * 检查图片格式是否被Java ImageIO支持
     */
    public static boolean isImageIOSupported(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        String extension = getFileExtension(filename).toLowerCase();
        // Java ImageIO 原生支持的格式
        return extension.equals(".jpg") || extension.equals(".jpeg") || 
               extension.equals(".png") || extension.equals(".gif") || 
               extension.equals(".bmp");
    }

    /**
     * 获取文件的MIME类型
     */
    public static String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".bmp":
                return "image/bmp";
            case ".webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 检查图片格式是否支持缩略图生成
     */
    public static boolean isThumbnailSupported(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        String extension = getFileExtension(filename).toLowerCase();
        // Thumbnailator 支持的格式
        return extension.equals(".jpg") || extension.equals(".jpeg") || 
               extension.equals(".png") || extension.equals(".gif") || 
               extension.equals(".bmp");
    }
}
