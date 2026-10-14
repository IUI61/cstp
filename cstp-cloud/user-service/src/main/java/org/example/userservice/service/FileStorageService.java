package org.example.userservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    // 批量校验图片类型，供"先整体校验、再逐个落盘"的场景使用
    void validateImages(MultipartFile[] files);
    // 校验并保存图片，返回可直接访问的相对 URL（如 /uploads/goods/2026-09-13/xxx.png）
    String storeImage(MultipartFile file, String dir);
}
