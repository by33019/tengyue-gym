package com.gym.service.impl;

import com.gym.service.FileService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket:gym-avatars}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO 初始化失败", e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        return doUpload(file, false);
    }

    @Override
    public String uploadWithWatermark(MultipartFile file) {
        return doUpload(file, true);
    }

    private String doUpload(MultipartFile file, boolean watermark) {
        try {
            String ext = getExtension(file.getOriginalFilename());
            String objectName = UUID.randomUUID().toString().substring(0, 16) + "." + ext;

            byte[] fileBytes;
            long fileSize;
            String contentType;

            if (watermark && isImage(ext)) {
                BufferedImage original = ImageIO.read(file.getInputStream());
                if (original != null) {
                    BufferedImage watermarked = addWatermark(original);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(watermarked, ext.equals("png") ? "png" : "jpg", baos);
                    fileBytes = baos.toByteArray();
                    fileSize = fileBytes.length;
                    contentType = "image/" + (ext.equals("png") ? "png" : "jpeg");
                } else {
                    fileBytes = file.getBytes();
                    fileSize = file.getSize();
                    contentType = file.getContentType();
                }
            } else {
                fileBytes = file.getBytes();
                fileSize = file.getSize();
                contentType = file.getContentType();
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(fileBytes), fileSize, -1)
                            .contentType(contentType)
                            .build());

            return endpoint + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    private BufferedImage addWatermark(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(source, 0, 0, null);

        // 水印文字
        String watermark = "腾跃健身 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int fontSize = Math.max(14, Math.min(width, height) / 25);
        g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, fontSize));
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(watermark);
        int textHeight = fm.getHeight();

        // 右下角
        int x = width - textWidth - 20;
        int y = height - textHeight;

        // 添加半透明阴影
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.drawString(watermark, x + 1, y + 1);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.drawString(watermark, x, y);

        g2d.dispose();
        return result;
    }

    private boolean isImage(String ext) {
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
