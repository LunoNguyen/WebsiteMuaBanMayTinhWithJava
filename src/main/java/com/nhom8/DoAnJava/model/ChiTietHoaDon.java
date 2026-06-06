package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CHITIETHOADON")
@IdClass(ChiTietHoaDonId.class) // Lớp đại diện cho composite key
public class ChiTietHoaDon {
    @Id
    @Column(name = "MASP")
    private String maSP;

    @Id
    @Column(name = "MAHD")
    private String maHD;

    @Column(name = "SOLUONGSP_HD")
    private String soLuongSP_HD;

    @Column(name = "THANHTIEN")
    private BigDecimal thanhTien;

    @ManyToOne
    @JoinColumn(name = "MAHD", insertable = false, updatable = false)
    private HoaDon hoaDon;

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public String getSoLuongSP_HD() {
        return soLuongSP_HD;
    }

    public void setSoLuongSP_HD(String soLuongSP_HD) {
        this.soLuongSP_HD = soLuongSP_HD;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    
}
