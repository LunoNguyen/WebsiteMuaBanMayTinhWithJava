package com.nhom8.DoAnJava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.ChucVu;

@Repository
public interface ChucVuRepository extends JpaRepository<ChucVu, String> {
    // Không cần viết thêm hàm gì ở đây, JpaRepository đã có sẵn hàm findAll() rồi
}