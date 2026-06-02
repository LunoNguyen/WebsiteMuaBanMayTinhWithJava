package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
//import java.util.List;

@Entity
@Table(name = "CHUCVU")
public class ChucVu {
    @Id
    @Column(name = "MACV")
    private String maCV;

    @Column(name = "TENCV")
    private String tenCV;

    public String getMaCV() {
        return maCV;
    }

    public void setMaCV(String maCV) {
        this.maCV = maCV;
    }

    public String getTenCV() {
        return tenCV;
    }

    public void setTenCV(String tenCV) {
        this.tenCV = tenCV;
    }



}
