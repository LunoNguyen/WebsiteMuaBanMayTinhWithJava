package com.nhom8.DoAnJava.repository;

import com.nhom8.DoAnJava.model.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {
    
    // Hàm dùng cho xử lý Đăng nhập
    Optional<TaiKhoan> findByEmailTKAndMatKhau(String emailTK, String matKhau);

    // Hàm dùng cho Đăng ký (kiểm tra xem email đã tồn tại chưa)
    Optional<TaiKhoan> findByEmailTK(String emailTK);

    // Hàm dùng để tìm mã tài khoản lớn nhất (dùng cho việc tự động sinh mã mới)
    TaiKhoan findFirstByMaTKStartingWithOrderByMaTKDesc(String prefix);
}