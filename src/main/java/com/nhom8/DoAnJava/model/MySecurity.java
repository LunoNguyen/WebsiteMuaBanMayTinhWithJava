package com.nhom8.DoAnJava.model;

import org.springframework.util.DigestUtils;

public class MySecurity {
    
    /**
     * Hàm băm mật khẩu bằng thuật toán MD5 (Tương thích 100% với database C# cũ)
     */
    public static String hashMD5(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // Spring Boot DigestUtils trả về chữ thường, ta cần .toUpperCase() để giống C#
        return DigestUtils.md5DigestAsHex(text.getBytes()).toUpperCase();
    }
}