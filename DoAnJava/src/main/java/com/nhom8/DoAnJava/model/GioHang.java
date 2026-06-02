package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GIOHANG")
public class GioHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MATK")
    private String maTK;

    @Column(name = "MASP")
    private String maSP;

    @Column(name = "SOLUONG")
    private Integer soLuong;

    @Column(name = "NGAYTHEM")
    private LocalDateTime ngayThem;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public LocalDateTime getNgayThem() {
        return ngayThem;
    }

    public void setNgayThem(LocalDateTime ngayThem) {
        this.ngayThem = ngayThem;
    }

    
}
