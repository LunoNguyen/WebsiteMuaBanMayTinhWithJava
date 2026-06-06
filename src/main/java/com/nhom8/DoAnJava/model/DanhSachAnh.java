package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;

@Entity
@Table(name = "DANHSACHANH")
public class DanhSachAnh {
    @Id
    @Column(name = "MADSA", length = 10, nullable = false)
    private String maDsa;

    @ManyToOne
    @JoinColumn(name = "MASP", referencedColumnName = "MASP", nullable = false)
    private SanPham sanPham;

    @Column(name = "TENANH", length = 100)
    private String tenAnh;

    // Getters and Setters
    public String getMaDsa() {
        return maDsa;
    }

    public void setMaDsa(String maDsa) {
        this.maDsa = maDsa;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public String getTenAnh() {
        return tenAnh;
    }

    public void setTenAnh(String tenAnh) {
        this.tenAnh = tenAnh;
    }
}
