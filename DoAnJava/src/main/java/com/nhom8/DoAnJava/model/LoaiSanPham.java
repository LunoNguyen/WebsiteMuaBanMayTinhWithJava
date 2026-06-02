package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
@Entity
@Table(name = "LOAISANPHAM")
public class LoaiSanPham {

    @Id
    @Column(name = "MALOAI")
    private String maLoai;

    @Column(name = "TENLOAI")
    private String tenLoai;

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    
}