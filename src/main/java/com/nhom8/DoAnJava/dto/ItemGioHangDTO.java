package com.nhom8.DoAnJava.dto;

public class ItemGioHangDTO {
    private String iMaSP;
    private String sTenSP;
    private String sAnhBia;
    private Double dDonGia;
    private Integer iSoLuong;

    public ItemGioHangDTO() {}

    public ItemGioHangDTO(String maSP, String tenSP, String anhBia, Double donGia) {
        this.iMaSP = maSP;
        this.sTenSP = tenSP;
        this.sAnhBia = (anhBia != null && !anhBia.isEmpty()) ? anhBia : "no-image.jpg";
        this.dDonGia = donGia;
        this.iSoLuong = 1;
    }

    public Double getDThanhTien() {
        return (this.iSoLuong != null && this.dDonGia != null) ? (this.iSoLuong * this.dDonGia) : 0.0;
    }

    public String getiMaSP() {
        return iMaSP;
    }

    public void setiMaSP(String iMaSP) {
        this.iMaSP = iMaSP;
    }

    public String getsTenSP() {
        return sTenSP;
    }

    public void setsTenSP(String sTenSP) {
        this.sTenSP = sTenSP;
    }

    public String getsAnhBia() {
        return sAnhBia;
    }

    public void setsAnhBia(String sAnhBia) {
        this.sAnhBia = sAnhBia;
    }

    public Double getdDonGia() {
        return dDonGia;
    }

    public void setdDonGia(Double dDonGia) {
        this.dDonGia = dDonGia;
    }

    public Integer getiSoLuong() {
        return iSoLuong;
    }

    public void setiSoLuong(Integer iSoLuong) {
        this.iSoLuong = iSoLuong;
    }

    
}
