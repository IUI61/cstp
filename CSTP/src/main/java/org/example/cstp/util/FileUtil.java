package org.example.cstp.util;

import org.example.cstp.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

public class FileUtil {
    public static String imageUpload(MultipartFile file, String dir) {
        // 提取扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        //生成唯一名
        String fileName = UUID.randomUUID() + ext;

        //存文件
        String dateDir = LocalDate.now().toString();
        String projectRoot = System.getProperty("user.dir");
        String uploadRoot = new File(projectRoot).getParent();
        String filePath = uploadRoot + "/uploads/" + dir +  "/" + dateDir + "/" + fileName;
        try {
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();  // 递归创建所有不存在的父目录
            }
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(500, "图片保存失败");
        }
        return "/uploads/" + dir + "/" + dateDir + "/" + fileName;
    }
}
