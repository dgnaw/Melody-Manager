package com.project.mange.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Lấy đường dẫn thư mục uploads
        Path uploadDir = Paths.get("uploads");

        // 2. Tạo đường dẫn URI chuẩn (file:///...) tự động theo hệ điều hành
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // Mẹo: Dùng toUri().toString() để Java tự xử lý dấu / hay \ và thêm prefix file:
        // Nó sẽ ra dạng: file:/C:/Users/YourProject/uploads/ (Windows) hoặc file:/home/user/uploads/ (Linux)
        String resourceLocation = uploadDir.toUri().toString();

        System.out.println("==================================================");
        System.out.println("DEBUG: Cấu hình Resource Handler");
        System.out.println(" - Thư mục vật lý: " + uploadPath);
        System.out.println(" - Resource URL:   " + resourceLocation);
        System.out.println("==================================================");

        registry.addResourceHandler("/files/**")
                .addResourceLocations(resourceLocation);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // Cho phép mọi nguồn (Frontend)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}