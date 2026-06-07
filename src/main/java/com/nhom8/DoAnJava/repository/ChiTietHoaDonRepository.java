package com.nhom8.DoAnJava.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Class chứa 2 ID

import com.nhom8.DoAnJava.model.ChiTietHoaDon;
import com.nhom8.DoAnJava.model.ChiTietHoaDonId;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, ChiTietHoaDonId> {
    List<ChiTietHoaDon> findByMaHD(String maHD);
    boolean existsByMaSP(String maSP);
}
