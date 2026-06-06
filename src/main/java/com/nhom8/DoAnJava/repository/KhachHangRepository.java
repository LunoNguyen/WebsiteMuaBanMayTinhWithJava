package com.nhom8.DoAnJava.repository;

import com.nhom8.DoAnJava.model.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, String> {
    
    KhachHang findFirstByMaKHStartingWithOrderByMaKHDesc(String prefix);
    
}