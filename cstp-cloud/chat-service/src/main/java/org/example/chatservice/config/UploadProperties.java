package org.example.chatservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 上传目录配置：读取 application.yml 中 chatservice.upload 前缀的配置
 * 作用：上传逻辑和静态资源映射统一从这里取路径，避免各自拼 user.dir
 */
@Data
@Component
@ConfigurationProperties(prefix = "chatservice.upload")
public class UploadProperties {
    /** 上传根目录，application.yml 中配置；这里只是兜底默认值 */
    private String baseDir = "uploads";

    // 统一出口：绝对化 + normalize，避免依赖启动目录、也杜绝 .. 路径
    public Path basePath() {
        return Paths.get(baseDir).toAbsolutePath().normalize();
    }
}
