package com.nhom8.DoAnJava.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom8.DoAnJava.model.KhuyenMai;

@Repository
public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, String> {
    KhuyenMai findTopByOrderByMaKMDesc();
    Optional<KhuyenMai> findByMaKMIgnoreCase(String maKM);
    Optional<KhuyenMai> findByMaHD(String maHD);

    @Query("SELECT k FROM KhuyenMai k ORDER BY k.maKM DESC")
    List<KhuyenMai> findAllForQuanLy();

    @Query(value = "SELECT COUNT(*) FROM CT_KHUYENMAI WHERE MAKM = :maKM", nativeQuery = true)
    long countChiTietByMaKM(@Param("maKM") String maKM);

    @Query(value = "SELECT MASP FROM CT_KHUYENMAI WHERE MAKM = :maKM", nativeQuery = true)
    List<String> findMaSPApDungByMaKM(@Param("maKM") String maKM);
}
