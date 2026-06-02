package com.nhom8.DoAnJava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.LoaiSanPham;

@Repository
public interface LoaiSanPhamRepository extends JpaRepository<LoaiSanPham, String> {
    
}