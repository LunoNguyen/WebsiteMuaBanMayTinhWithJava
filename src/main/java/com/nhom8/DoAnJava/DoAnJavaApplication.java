package com.nhom8.DoAnJava;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DoAnJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoAnJavaApplication.class, args);
    }

    @Bean
    public CommandLineRunner testDatabaseConnection(DataSource dataSource) {
        return args -> {
            System.out.println("\n=== KIỂM TRA KẾT NỐI DATABASE SẴN CÓ ===");
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("Trạng thái: KẾT NỐI THÀNH CÔNG!");
                System.out.println("Đường dẫn (URL): " + connection.getMetaData().getURL());
                System.out.println("Hệ quản trị DB: " + connection.getMetaData().getDatabaseProductName());
            } catch (Exception e) {
                System.err.println("Trạng thái: KẾT NỐI THẤT BẠI!");
                System.err.println("Chi tiết lỗi: " + e.getMessage());
            }
            System.out.println("=========================================\n");
        };
    }   
}