package com.nhom8.DoAnJava.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "NHACUNGCAP")
public class NhaCungCap {

    @Id
    @Column(name = "MANCC")
    private String maNCC;

    @Column(name = "TENNCC")
    private String tenNCC;

    @Column(name = "DIACHI_NCC")
    private String diaChiNCC;

    @Column(name = "SDT_NCC")
    private String sdtNCC;

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }

    public String getDiaChiNCC() {
        return diaChiNCC;
    }

    public void setDiaChiNCC(String diaChiNCC) {
        this.diaChiNCC = diaChiNCC;
    }

    public String getSdtNCC() {
        return sdtNCC;
    }

    public void setSdtNCC(String sdtNCC) {
        this.sdtNCC = sdtNCC;
    }

    
}