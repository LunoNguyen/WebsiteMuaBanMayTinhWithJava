package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PHIEUNHAPHANG")
public class PhieuNhapHang {

    @Id
    @Column(name = "MAPNH", length = 10)
    private String maPNH;

    @Column(name = "MANCC", length = 10, nullable = false)
    private String maNCC;

    @Column(name = "NGAYGIAO")
    private LocalDateTime ngayGiao;

    @Column(name = "NGAYNHAN")
    private LocalDateTime ngayNhan;

    @Column(name = "TRANGTHAI_THANHTOAN_NH", length = 100)
    private String trangThaiThanhToan;

    @Column(name = "THUE_VAT", precision = 18, scale = 2)
    private BigDecimal thueVAT;

    @Column(name = "CHIETKHAU", precision = 18, scale = 2)
    private BigDecimal chietKhau;

    @Column(name = "TONGCONG_PNH", length = 100)
    private String tongCong;

    // Quan hệ đọc thêm thông tin NCC (không insert)
    @ManyToOne
    @JoinColumn(name = "MANCC", insertable = false, updatable = false)
    private NhaCungCap nhaCungCap;

    // ===== Getters & Setters =====

    public String getMaPNH() { return maPNH; }
    public void setMaPNH(String maPNH) { this.maPNH = maPNH; }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public LocalDateTime getNgayGiao() { return ngayGiao; }
    public void setNgayGiao(LocalDateTime ngayGiao) { this.ngayGiao = ngayGiao; }

    public LocalDateTime getNgayNhan() { return ngayNhan; }
    public void setNgayNhan(LocalDateTime ngayNhan) { this.ngayNhan = ngayNhan; }

    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }

    public BigDecimal getThueVAT() { return thueVAT; }
    public void setThueVAT(BigDecimal thueVAT) { this.thueVAT = thueVAT; }

    public BigDecimal getChietKhau() { return chietKhau; }
    public void setChietKhau(BigDecimal chietKhau) { this.chietKhau = chietKhau; }

    public String getTongCong() { return tongCong; }
    public void setTongCong(String tongCong) { this.tongCong = tongCong; }

    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }
}