package org.example.userservice.service.impl;

import org.example.userservice.config.UploadProperties;
import org.example.userservice.exception.BusinessException;
import org.example.userservice.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 文件存储实现：替代原 FileUtil
 * 1. 类型校验：不信任扩展名和请求头 Content-Type（都可伪造），只认文件头魔数
 * 2. 路径管理：上传目录来自 UploadProperties，与启动目录（user.dir）解耦
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final UploadProperties uploadProperties;

    public FileStorageServiceImpl(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    // 全部文件先统一校验，避免"第 3 张非法时前 2 张已落盘"的孤儿文件
    @Override
    public void validateImages(MultipartFile[] files) {
        for (MultipartFile file : files) {
            detectImageType(file);
        }
    }

    // 校验并保存，返回可直接访问的相对 URL
    @Override
    public String storeImage(MultipartFile file, String dir) {
        // 落盘扩展名用魔数识别出的真实类型，不用客户端给的文件名后缀（改名绕过无效）
        String ext = detectImageType(file);
        String fileName = UUID.randomUUID() + ext;
        String relativePath = dir + "/" + LocalDate.now() + "/" + fileName;
        Path dest = uploadProperties.basePath().resolve(relativePath).normalize();
        try {
            Files.createDirectories(dest.getParent());
            try (InputStream in = file.getInputStream()) {
                // 不用 transferTo：传相对路径 File 时会被容器按临时目录解析，NIO 拷贝更可控
                Files.copy(in, dest);
            }
        } catch (IOException e) {
            throw new BusinessException(500, "图片保存失败");
        }
        return "/uploads/" + relativePath;
    }

    // 通过文件头（魔数）识别真实图片类型，不在白名单内直接拒收，返回归一化后的扩展名
    private String detectImageType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "图片不能为空");
        }
        byte[] head = new byte[12];
        int len;
        try (InputStream in = file.getInputStream()) {
            len = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            throw new BusinessException(400, "图片读取失败");
        }
        // JPEG: FF D8 FF
        if (len >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
            return ".jpg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (len >= 8 && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G'
                && (head[4] & 0xFF) == 0x0D && (head[5] & 0xFF) == 0x0A
                && (head[6] & 0xFF) == 0x1A && (head[7] & 0xFF) == 0x0A) {
            return ".png";
        }
        // GIF: GIF87a / GIF89a
        if (len >= 6 && head[0] == 'G' && head[1] == 'I' && head[2] == 'F' && head[3] == '8'
                && (head[4] == '7' || head[4] == '9') && head[5] == 'a') {
            return ".gif";
        }
        // WEBP: 前 4 字节 RIFF，第 8-11 字节 WEBP
        if (len >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') {
            return ".webp";
        }
        throw new BusinessException(400, "仅支持 jpg/png/gif/webp 格式的图片");
    }
}
