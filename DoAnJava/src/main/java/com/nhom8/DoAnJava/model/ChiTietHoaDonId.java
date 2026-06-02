package com.nhom8.DoAnJava.model;

import java.io.Serializable;
import java.util.Objects;

public class ChiTietHoaDonId implements Serializable {
    
    private String maSP;
    private String maHD;

    // Bắt buộc phải có constructor rỗng
    public ChiTietHoaDonId() {
    }

    public ChiTietHoaDonId(String maSP, String maHD) {
        this.maSP = maSP;
        this.maHD = maHD;
    }

    // Getters và Setters
    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    // Bắt buộc override equals() và hashCode() để JPA có thể so sánh các ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietHoaDonId that = (ChiTietHoaDonId) o;
        return Objects.equals(maSP, that.maSP) && Objects.equals(maHD, that.maHD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maSP, maHD);
    }
}
