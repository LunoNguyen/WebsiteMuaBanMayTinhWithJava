package com.nhom8.DoAnJava.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đường dẫn đến thư mục ngoài máy tính lưu trữ ảnh upload
        Path uploadDir = Paths.get("uploads/HinhAnhPhongVu");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // Khi trình duyệt gọi: /Content/HinhAnhPhongVu/ten_anh.jpg
        // Spring Boot sẽ tự động cấu hình đọc trực tiếp trong thư mục vật lý ngoài máy tính
        registry.addResourceHandler("/Content/HinhAnhPhongVu/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}