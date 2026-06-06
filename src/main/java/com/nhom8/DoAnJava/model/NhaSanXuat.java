package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;

@Entity
@Table(name = "NHASANXUA")
public class NhaSanXuat {

    @Id
    @Column(name = "MANSX")
    private String maNSX;

    @Column(name = "TENNSX")
    private String tenNSX;

    public String getMaNSX() {
        return maNSX;
    }

    public void setMaNSX(String maNSX) {
        this.maNSX = maNSX;
    }

    public String getTenNSX() {
        return tenNSX;
    }

    public void setTenNSX(String tenNSX) {
        this.tenNSX = tenNSX;
    }

    
}