package com.nhom8.DoAnJava.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "KHUYENMAI")
public class KhuyenMai {
    @Id
    @Column(name = "MAKM")
    private String maKM;

    @Column(name = "MAHD")
    private String maHD;

    @Column(name = "TENKM")
    private String tenKM;

    @Column(name = "PHANTRAMKM")
    private String phanTramKM;

    @Column(name = "SOTIENTOIDA_KM")
    private BigDecimal soTienToiDaKM;

    @Column(name = "SOTIENTOITHIEU_NHANKM")
    private BigDecimal soTienToiThieuNhanKM;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "NGAYBD")
    private LocalDateTime ngayBD;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "NGAYKT")
    private LocalDateTime ngayKT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAHD", insertable = false, updatable = false)
    private HoaDon hoaDon;

    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public String getTenKM() {
        return tenKM;
    }

    public void setTenKM(String tenKM) {
        this.tenKM = tenKM;
    }

    public String getPhanTramKM() {
        return phanTramKM;
    }

    public void setPhanTramKM(String phanTramKM) {
        this.phanTramKM = phanTramKM;
    }

    public BigDecimal getSoTienToiDaKM() {
        return soTienToiDaKM;
    }

    public void setSoTienToiDaKM(BigDecimal soTienToiDaKM) {
        this.soTienToiDaKM = soTienToiDaKM;
    }

    public BigDecimal getSoTienToiThieuNhanKM() {
        return soTienToiThieuNhanKM;
    }

    public void setSoTienToiThieuNhanKM(BigDecimal soTienToiThieuNhanKM) {
        this.soTienToiThieuNhanKM = soTienToiThieuNhanKM;
    }

    public LocalDateTime getNgayBD() {
        return ngayBD;
    }

    public void setNgayBD(LocalDateTime ngayBD) {
        this.ngayBD = ngayBD;
    }

    public LocalDateTime getNgayKT() {
        return ngayKT;
    }

    public void setNgayKT(LocalDateTime ngayKT) {
        this.ngayKT = ngayKT;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }
}
