package com.nhom8.DoAnJava.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "MOTA")
public class MoTa {

    @Id
    @Column(name = "MAMT")
    private String maMT;

    @Column(name = "MASP")
    private String maSP;

    @Column(name = "RAM")
    private String ram;

    @Column(name = "CPU")
    private String cpu;

    @Column(name = "ROM")
    private String rom;

    @Column(name = "MANHINH")
    private String manHinh;

    @Column(name = "VGA")
    private String vga;

    @Column(name = "KHAC")
    private String khac;

    public String getMaMT() {
        return maMT;
    }

    public void setMaMT(String maMT) {
        this.maMT = maMT;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRom() {
        return rom;
    }

    public void setRom(String rom) {
        this.rom = rom;
    }

    public String getManHinh() {
        return manHinh;
    }

    public void setManHinh(String manHinh) {
        this.manHinh = manHinh;
    }

    public String getVga() {
        return vga;
    }

    public void setVga(String vga) {
        this.vga = vga;
    }

    public String getKhac() {
        return khac;
    }

    public void setKhac(String khac) {
        this.khac = khac;
    }

    @ManyToOne
    @JoinColumn(name = "masp", insertable = false, updatable = false)
    private SanPham sanPham;

    
}