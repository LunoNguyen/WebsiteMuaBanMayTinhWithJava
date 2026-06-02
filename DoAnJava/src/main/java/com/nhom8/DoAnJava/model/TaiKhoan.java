package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
@Entity
@Table(name = "TAIKHOAN")
public class TaiKhoan {

    @Id
    @Column(name = "MATK")
    private String maTK;

    @Column(name = "MANV")
    private String maNV;

    @Column(name = "MAKH")
    private String maKH;

    @Column(name = "EMAIL_TK")
    private String emailTK;

    @Column(name = "MATKHAU")
    private String matKhau;

    @Column(name = "LOAI_TAIKHOAN")
    private String loaiTaiKhoan;

    // --- Getters và Setters ---
    
    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getEmailTK() {
        return emailTK;
    }

    public void setEmailTK(String emailTK) {
        this.emailTK = emailTK;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getLoaiTaiKhoan() {
        return loaiTaiKhoan;
    }

    public void setLoaiTaiKhoan(String loaiTaiKhoan) {
        this.loaiTaiKhoan = loaiTaiKhoan;
    }
}