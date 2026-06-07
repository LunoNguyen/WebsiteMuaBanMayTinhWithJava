package com.nhom8.DoAnJava.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
//import java.util.List;

@Entity
@Table(name = "CHUCVU") // Tên bảng trong Database
public class ChucVu {

    @Id
    @Column(name = "MACV")
    private String maCV;

    @Column(name = "TENCV")
    private String tenCV;

    // Nếu bạn đang dùng @Data của Lombok thì có thể xóa các hàm Getter/Setter bên dưới đi
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
