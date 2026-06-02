package com.nhom8.DoAnJava.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.GioHang;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    // Custom query: Lấy toàn bộ sản phẩm trong giỏ hàng của một tài khoản
    List<GioHang> findByMaTK(String maTK);
    GioHang findByMaTKAndMaSP(String maTK, String maSP);
}
