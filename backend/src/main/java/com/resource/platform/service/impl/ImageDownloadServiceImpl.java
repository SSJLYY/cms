package com.resource.platform.service.impl;

import com.resource.platform.entity.ResourceImage;
import com.resource.platform.mapper.ResourceImageMapper;
import com.resource.platform.service.ImageDownloadService;
import com.resource.platform.service.ImageService;
import com.resource.platform.util.FileValidationUtil;
import com.resource.platform.vo.ImageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ImageDownloadServiceImpl implements ImageDownloadService {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ResourceImageMapper resourceImageMapper;

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int CONNECT_TIMEOUT = 10000; // 10秒
    private static final int READ_TIMEOUT = 30000; // 30秒

    @Override
    public ImageVO downloadAndUploadImage(String imageUrl, Long uploaderId) {
        int retries = 0;
        Exception lastException = null;

        while (retries < MAX_RETRIES) {
            try {
                log.info("下载图片: url={}, retry={}", imageUrl, retries);

                // 下载图片到临时存储
                byte[] imageData = downloadImage(imageUrl);

                // 验证图片格式和大小
                validateImage(imageData, imageUrl);

                // 转换为MultipartFile
                MultipartFile multipartFile = createMultipartFile(imageData, imageUrl);

                // 上传到平台
                ImageVO imageVO = imageService.uploadImage(multipartFile, uploaderId);

                log.info("图片上传成功: url={}, imageId={}", imageUrl, imageVO.getId());
                return imageVO;

            } catch (Exception e) {
                lastException = e;
                retries++;
                log.error("下载图片失败: url={}, retry={}/{}", imageUrl, retries, MAX_RETRIES, e);

                if (retries < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("下载图片最终失败: url={}, retries={}", imageUrl, MAX_RETRIES, lastException);
        return null;
    }

    @Override
    public List<ImageVO> downloadAndUploadImages(List<String> imageUrls, Long uploaderId) {
        List<ImageVO> uploadedImages = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            try {
                ImageVO imageVO = downloadAndUploadImage(imageUrl, uploaderId);
                if (imageVO != null) {
                    uploadedImages.add(imageVO);
                }
            } catch (Exception e) {
                log.error("批量下载图片失败，继续处理其他图片: url={}", imageUrl, e);
                // 单个图片失败不影响其他图片处理
            }
        }

        log.info("批量下载图片完成: total={}, success={}", imageUrls.size(), uploadedImages.size());
        return uploadedImages;
    }

    @Override
    public void associateImagesToResource(Long resourceId, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        for (Long imageId : imageIds) {
            ResourceImage resourceImage = new ResourceImage();
            resourceImage.setResourceId(resourceId);
            resourceImage.setImageId(imageId);
            resourceImage.setCreateTime(LocalDateTime.now());
            resourceImageMapper.insert(resourceImage);
        }

        log.info("关联图片到资源: resourceId={}, imageCount={}", resourceId, imageIds.size());
    }

    /**
     * 下载图片
     */
    private byte[] downloadImage(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP错误: " + responseCode);
        }

        // 检查Content-Type
        String contentType = conn.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new Exception("不是图片类型: " + contentType);
        }

        // 检查Content-Length
        int contentLength = conn.getContentLength();
        if (contentLength > MAX_IMAGE_SIZE) {
            throw new Exception("图片过大: " + contentLength + " bytes");
        }

        // 读取图片数据
        InputStream inputStream = conn.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        int totalBytes = 0;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            totalBytes += bytesRead;
            if (totalBytes > MAX_IMAGE_SIZE) {
                throw new Exception("图片过大: 超过" + MAX_IMAGE_SIZE + " bytes");
            }
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return outputStream.toByteArray();
    }

    /**
     * 验证图片格式和大小
     */
    private void validateImage(byte[] imageData, String imageUrl) throws Exception {
        if (imageData == null || imageData.length == 0) {
            throw new Exception("图片数据为空");
        }

        if (imageData.length > MAX_IMAGE_SIZE) {
            throw new Exception("图片过大: " + imageData.length + " bytes");
        }

        // 检查图片格式（通过文件头）
        String format = detectImageFormat(imageData);
        if (format == null) {
            throw new Exception("不支持的图片格式");
        }

        log.debug("图片验证通过: url={}, format={}, size={}", imageUrl, format, imageData.length);
    }

    /**
     * 检测图片格式
     */
    private String detectImageFormat(byte[] data) {
        if (data.length < 4) {
            return null;
        }

        // JPEG
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) {
            return "jpg";
        }

        // PNG
        if (data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
            return "png";
        }

        // GIF
        if (data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46) {
            return "gif";
        }

        // WebP
        if (data.length >= 12 && 
            data[0] == 0x52 && data[1] == 0x49 && data[2] == 0x46 && data[3] == 0x46 &&
            data[8] == 0x57 && data[9] == 0x45 && data[10] == 0x42 && data[11] == 0x50) {
            return "webp";
        }

        return null;
    }

    /**
     * 创建MultipartFile
     */
    private MultipartFile createMultipartFile(byte[] data, String imageUrl) {
        String format = detectImageFormat(data);
        String filename = extractFilename(imageUrl, format);
        String contentType = "image/" + format;

        return new ByteArrayMultipartFile(data, filename, contentType);
    }
    
    /**
     * 自定义MultipartFile实现
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] data;
        private final String filename;
        private final String contentType;
        
        public ByteArrayMultipartFile(byte[] data, String filename, String contentType) {
            this.data = data;
            this.filename = filename;
            this.contentType = contentType;
        }
        
        @Override
        public String getName() {
            return "file";
        }
        
        @Override
        public String getOriginalFilename() {
            return filename;
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
        @Override
        public boolean isEmpty() {
            return data == null || data.length == 0;
        }
        
        @Override
        public long getSize() {
            return data.length;
        }
        
        @Override
        public byte[] getBytes() throws IOException {
            return data;
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }
        
        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(data);
            }
        }
    }

    /**
     * 从URL提取文件名
     */
    private String extractFilename(String imageUrl, String format) {
        try {
            String path = new URL(imageUrl).getPath();
            String filename = path.substring(path.lastIndexOf('/') + 1);
            
            // 如果文件名没有扩展名，添加扩展名
            if (!filename.contains(".")) {
                filename = filename + "." + format;
            }
            
            return filename;
        } catch (Exception e) {
            return "image_" + System.currentTimeMillis() + "." + format;
        }
    }
}
