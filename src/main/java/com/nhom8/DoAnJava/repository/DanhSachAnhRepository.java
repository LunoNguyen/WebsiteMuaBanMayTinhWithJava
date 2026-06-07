package com.nhom8.DoAnJava.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.DanhSachAnh;

@Repository
public interface DanhSachAnhRepository extends JpaRepository<DanhSachAnh, String> {
    DanhSachAnh findTopByOrderByMaDsaDesc();
    List<DanhSachAnh> findBySanPham_MaSP(String maSP);
    void deleteBySanPham_MaSP(String maSP);
}
