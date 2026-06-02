package com.nhom8.DoAnJava.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
@Entity
@Table(name = "SANPHAM")
public class SanPham {

    @Id
    @Column(name = "MASP")
    private String maSP;

    @Column(name = "NGAYCN")
    private LocalDateTime ngayCN;

    @Column(name = "MANCC")
    private String maNCC;

    @Column(name = "MALOAI")
    private String maLoai;

    @Column(name = "MANSX")
    private String maNSX;

    @Column(name = "MAPNH")
    private String maPNH;

    @Column(name = "TENSP")
    private String tenSP;

    @Column(name = "DONVT")
    private String donVT;

    @Column(name = "SOLUONGTON")
    private Integer soLuongTon;

    @Column(name = "DONGIA_SP")
    private BigDecimal donGiaSP;


    // Getters and Setters
    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }
    
    public LocalDateTime getNgayCN() { return ngayCN; }
    public void setNgayCN(LocalDateTime ngayCN) { this.ngayCN = ngayCN; }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public String getMaLoai() { return maLoai; }
    public void setMaLoai(String maLoai) { this.maLoai = maLoai; }

    public String getMaNSX() { return maNSX; }
    public void setMaNSX(String maNSX) { this.maNSX = maNSX; }

    public String getMaPNH() { return maPNH; }
    public void setMaPNH(String maPNH) { this.maPNH = maPNH; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public String getDonVT() { return donVT; }
    public void setDonVT(String donVT) { this.donVT = donVT; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public BigDecimal getDonGiaSP() { return donGiaSP; }
    public void setDonGiaSP(BigDecimal donGiaSP) { this.donGiaSP = donGiaSP; }

    // Thêm danh sách mô tả vào SanPham
    @OneToMany(mappedBy = "sanPham")
    private List<MoTa> moTas;

    public List<MoTa> getMoTas() {
        return moTas;
    }

    public void setMoTas(List<MoTa> moTas) {
        this.moTas = moTas;
    }

    @ManyToOne
    @JoinColumn(name = "MALOAI", insertable = false, updatable = false)
    private LoaiSanPham loaiSanPham; // Giúp lấy đối tượng Loại sản phẩm trực tiếp

    @ManyToOne
    @JoinColumn(name = "MANSX", insertable = false, updatable = false)
    private NhaSanXuat nhaSanXuat;   // Giúp lấy đối tượng Nhà sản xuất trực tiếp


    // Đừng quên bổ sung Getter và Setter cho 2 thuộc tính mới này ở phía dưới:
    public LoaiSanPham getLoaiSanPham() { return loaiSanPham; }
    public void setLoaiSanPham(LoaiSanPham loaiSanPham) { this.loaiSanPham = loaiSanPham; }

    public NhaSanXuat getNhaSanXuat() { return nhaSanXuat; }
    public void setNhaSanXuat(NhaSanXuat nhaSanXuat) { this.nhaSanXuat = nhaSanXuat; }

}