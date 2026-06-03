package com.nhom8.DoAnJava.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CAPNHATGIA")
public class CapNhatGia {
    @Id
    @Column(name = "NGAYCN")
    private LocalDateTime ngayCN;

    @Column(name = "DONGIA_CN")
    private BigDecimal donGiaCN;

    public LocalDateTime getNgayCN() {
        return ngayCN;
    }

    public void setNgayCN(LocalDateTime ngayCN) {
        this.ngayCN = ngayCN;
    }

    public BigDecimal getDonGiaCN() {
        return donGiaCN;
    }

    public void setDonGiaCN(BigDecimal donGiaCN) {
        this.donGiaCN = donGiaCN;
    }


}
