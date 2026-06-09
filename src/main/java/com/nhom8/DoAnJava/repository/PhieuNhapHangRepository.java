package com.nhom8.DoAnJava.repository;

import com.nhom8.DoAnJava.model.PhieuNhapHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuNhapHangRepository extends JpaRepository<PhieuNhapHang, String> {

    // Tìm phiếu có mã lớn nhất để auto-increment
    PhieuNhapHang findTopByOrderByMaPNHDesc();
}