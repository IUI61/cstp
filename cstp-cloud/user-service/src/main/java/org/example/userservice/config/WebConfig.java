package org.example.userservice.config;

import org.example.userservice.config.UploadProperties;
import org.example.userservice.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;
    private final UploadProperties uploadProperties;
    public WebConfig(LoginInterceptor loginInterceptor, UploadProperties uploadProperties) {
        this.loginInterceptor = loginInterceptor;
        this.uploadProperties = uploadProperties;
    }
    // 添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)// 注册拦截器
                .addPathPatterns("/**")// 拦截所有路径
                .excludePathPatterns("/user/login",
                        "/user/register",
                        "/uploads/**",
                        "/user/inner/**",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**");
    }
    // 配置静态资源映射：路径来源与上传逻辑保持一致（UploadProperties 统一提供）
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadProperties.basePath().toUri().toString();
        // Spring 要求 file: 资源地址以 / 结尾
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

}
