package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;

@Entity
@Table(name = "NHANVIEN")
public class NhanVien {

    @Id
    @Column(name = "MANV")
    private String maNV;

    @Column(name = "MACV")
    private String maCV;

    @Column(name = "TENNV")
    private String tenNV;

    @Column(name = "SDT_NV")
    private String sdtNV;

    @Column(name = "DIACHI_NV")
    private String diaChiNV;

    @Column(name = "EMAIL_NV")
    private String emailNV;

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaCV() {
        return maCV;
    }

    public void setMaCV(String maCV) {
        this.maCV = maCV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public String getSdtNV() {
        return sdtNV;
    }

    public void setSdtNV(String sdtNV) {
        this.sdtNV = sdtNV;
    }

    public String getDiaChiNV() {
        return diaChiNV;
    }

    public void setDiaChiNV(String diaChiNV) {
        this.diaChiNV = diaChiNV;
    }

    public String getEmailNV() {
        return emailNV;
    }

    public void setEmailNV(String emailNV) {
        this.emailNV = emailNV;
    }

    
}