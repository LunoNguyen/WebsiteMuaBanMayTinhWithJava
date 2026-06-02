package com.nhom8.DoAnJava.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhom8.DoAnJava.model.SanPham;
import com.nhom8.DoAnJava.repository.LoaiSanPhamRepository;
import com.nhom8.DoAnJava.repository.NhaSanXuatRepository;
import com.nhom8.DoAnJava.repository.SanPhamRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    // Nhớ thêm @Autowired cho 2 Repository này ở đầu class nếu chưa có
    @Autowired
    private NhaSanXuatRepository nhaSanXuatRepository;

    @Autowired
    private LoaiSanPhamRepository loaiSanPhamRepository;

    @GetMapping("/gioi-thieu")
    public String gioiThieu() {
        return "home/GioiThieu"; 
    }

    @GetMapping({"/", "/trang-chu"})
    public String trangChu(Model model) {
        // Tương đương: db.SANPHAMs.Take(12).Where(s => s.SOLUONGTON > 0).ToList();
        List<SanPham> dssp = sanPhamRepository.findTop12BySoLuongTonGreaterThan(0);
        model.addAttribute("dssp", dssp);

        // Bơm danh sách trực tiếp từ Database vào giao diện
        model.addAttribute("dsnsx", nhaSanXuatRepository.findAll());
        model.addAttribute("dslsp", loaiSanPhamRepository.findAll());

        return "home/TrangChu";
    }

    @GetMapping("/chi-tiet-san-pham/{id}")
    public String chiTietSanPham(@PathVariable String id, Model model) {
        SanPham sp = sanPhamRepository.findById(id).orElse(null);
        if (sp == null) {
            return "redirect:/trang-chu"; // HttpNotFound
        }
        
        List<SanPham> spCungNSX = sanPhamRepository.findByMaNSX(sp.getMaNSX());
        List<SanPham> spCungLSP = sanPhamRepository.findByMaLoai(sp.getMaLoai());

        model.addAttribute("sp", sp);
        model.addAttribute("SPcungNSX", spCungNSX);
        model.addAttribute("SPcungLSP", spCungLSP);

        return "home/ChiTietSanPham";
    }

    @PostMapping("/tim-kiem")
    public String xuLyTimKiem(@RequestParam("txtTuKhoa") String txtTuKhoa, Model model) {
        List<SanPham> dssp = sanPhamRepository.findByTenSPContainingAndSoLuongTonGreaterThanEqual(txtTuKhoa, 0);
        model.addAttribute("dssp", dssp);
        return "home/TrangChu";
    }

    @GetMapping("/logout")
    public String logOut(HttpSession session) {
        // Xóa Session tương đương Session.Clear()
        session.invalidate(); 
        return "redirect:/trang-chu";
    }

    // Bắt sự kiện click vào Thương Hiệu (Nhà Sản Xuất)
    @GetMapping("/tim-kiem/nsx/{id}")
    public String timKiemTheoNSX(@PathVariable("id") String id, Model model) {
        // Lấy danh sách sản phẩm ĐÃ LỌC từ Database
        model.addAttribute("dssp", sanPhamRepository.findByMaNSX(id));
        
        // Vẫn phải bơm lại 2 cục danh mục này để giao diện không bị trống
        model.addAttribute("dsnsx", nhaSanXuatRepository.findAll());
        model.addAttribute("dslsp", loaiSanPhamRepository.findAll());
        
        return "home/TrangChu";
    }

    // Bắt sự kiện click vào Loại Sản Phẩm
    @GetMapping("/tim-kiem/loai/{id}")
    public String timKiemTheoLoai(@PathVariable("id") String id, Model model) {
        // Lấy danh sách sản phẩm ĐÃ LỌC từ Database
        model.addAttribute("dssp", sanPhamRepository.findByMaLoai(id));
        
        model.addAttribute("dsnsx", nhaSanXuatRepository.findAll());
        model.addAttribute("dslsp", loaiSanPhamRepository.findAll());
        
        return "home/TrangChu";
    }
}