package com.nhom8.DoAnJava.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.SanPham;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, String> {
    
    // Tìm 12 sản phẩm có số lượng tồn lớn hơn tham số truyền vào
    List<SanPham> findTop12BySoLuongTonGreaterThan(Integer soLuongTon);

    // Ép Hibernate nạp kèm dữ liệu mô tả ngay khi lọc theo Nhà Sản Xuất
    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN FETCH s.moTas WHERE s.maNSX = :maNSX")
    List<SanPham> findByMaNSX(@Param("maNSX") String maNSX);

    // Ép Hibernate nạp kèm dữ liệu mô tả ngay khi lọc theo Loại Sản Phẩm
    @Query("SELECT DISTINCT s FROM SanPham s LEFT JOIN FETCH s.moTas WHERE s.maLoai = :maLoai")
    List<SanPham> findByMaLoai(@Param("maLoai") String maLoai);

    // Tìm kiếm sản phẩm theo tên (có chứa từ khóa) và số lượng tồn >= tham số
    List<SanPham> findByTenSPContainingAndSoLuongTonGreaterThanEqual(String tenSP, Integer soLuongTon);

    // Khai báo để Spring Boot tự động sinh câu lệnh: SELECT * FROM SanPham WHERE LOWER(tensp) = LOWER(?)
    Optional<SanPham> findByTenSPIgnoreCase(String tenSP);

    Optional<SanPham> findTopByOrderByMaSPDesc();


}