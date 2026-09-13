package org.example.cstp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.example.cstp.interceptor.LoginInterceptor;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;
    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }
    // 添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)// 注册拦截器
                .addPathPatterns("/**")// 拦截所有路径
                .excludePathPatterns("/user/login",
                        "/user/register",
                        "/uploads/**",
                        "/goods/show",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/ws/**");
    }
    // 配置静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = new File(System.getProperty("user.dir")).getParent() + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

}
