package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "HOADON")
public class HoaDon {
    @Id
    @Column(name = "MAHD")
    private String maHD;

    @Column(name = "NGAYLAP")
    private LocalDateTime ngayLap;

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getTrangThaiTT() {
        return trangThaiTT;
    }

    public void setTrangThaiTT(String trangThaiTT) {
        this.trangThaiTT = trangThaiTT;
    }

    public BigDecimal getTongTienHD() {
        return tongTienHD;
    }

    public void setTongTienHD(BigDecimal tongTienHD) {
        this.tongTienHD = tongTienHD;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public List<ChiTietHoaDon> getChiTietHoaDons() {
        return chiTietHoaDons;
    }

    public void setChiTietHoaDons(List<ChiTietHoaDon> chiTietHoaDons) {
        this.chiTietHoaDons = chiTietHoaDons;
    }

    @Column(name = "TRANGTHAITT")
    private String trangThaiTT;

    @Column(name = "TONGTIEN_HD")
    private BigDecimal tongTienHD;

    @ManyToOne
    @JoinColumn(name = "MAKH")
    private KhachHang khachHang;

    // Giả định bạn có entity NhanVien tương ứng với cột MANV
    @Column(name = "MANV")
    private String maNV; 

    @OneToMany(mappedBy = "hoaDon")
    private List<ChiTietHoaDon> chiTietHoaDons;

    
}
