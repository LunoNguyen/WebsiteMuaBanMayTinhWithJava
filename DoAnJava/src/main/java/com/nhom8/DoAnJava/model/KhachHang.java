package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "KHACHHANG")
public class KhachHang {
    @Id
    @Column(name = "MAKH")
    private String maKH;

    @Column(name = "TENKH")
    private String tenKH;

    @Column(name = "SDT_KH")
    private String sdtKH;

    @Column(name = "DIACHI_KH")
    private String diaChiKH;

    @Column(name = "EMAIL_KH")
    private String emailKH;

    @OneToMany(mappedBy = "khachHang")
    private List<HoaDon> hoaDons;

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getSdtKH() {
        return sdtKH;
    }

    public void setSdtKH(String sdtKH) {
        this.sdtKH = sdtKH;
    }

    public String getDiaChiKH() {
        return diaChiKH;
    }

    public void setDiaChiKH(String diaChiKH) {
        this.diaChiKH = diaChiKH;
    }

    public String getEmailKH() {
        return emailKH;
    }

    public void setEmailKH(String emailKH) {
        this.emailKH = emailKH;
    }

    public List<HoaDon> getHoaDons() {
        return hoaDons;
    }

    public void setHoaDons(List<HoaDon> hoaDons) {
        this.hoaDons = hoaDons;
    }

    

}