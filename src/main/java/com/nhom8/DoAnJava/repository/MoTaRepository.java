package com.nhom8.DoAnJava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nhom8.DoAnJava.model.MoTa;

@Repository
public interface MoTaRepository extends JpaRepository<MoTa, String> {
    // Interface này giúp thao tác lưu/sửa cấu hình chi tiết sản phẩm xuống Database
}