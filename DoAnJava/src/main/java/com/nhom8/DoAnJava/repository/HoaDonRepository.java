package com.nhom8.DoAnJava.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.HoaDon;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, String> {
    
    // Tìm danh sách hóa đơn dựa trên Mã Khách Hàng và sắp xếp ngày lập mới nhất lên đầu
    List<HoaDon> findByKhachHang_MaKHOrderByNgayLapDesc(String maKH);
    HoaDon findFirstByOrderByMaHDDesc();
    @Query("SELECT h FROM HoaDon h WHERE MONTH(h.ngayLap) = :thang AND YEAR(h.ngayLap) = :nam")
    List<HoaDon> findByThangAndNam(@Param("thang") int thang, @Param("nam") int nam);
}